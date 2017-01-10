(ns afrey.boot-asset-fingerprint.impl
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn asset-full-path
  "Return the full path of an asset, taking into account relative and absolute paths.

  Examples:
  (asset-full-path \"foo.txt\" \"\") => \"foo.txt\"
  (asset-full-path \"/foo.txt\" \"\") => \"foo.txt\"
  (asset-full-path \"/foo.txt\" \"parent\") => \"foo.txt\"
  (asset-full-path \"foo.txt\" \"parent\") => \"parent/foo.txt\""

  [path relative-root]
  (let [separator (java.io.File/separator)]
    (cond
      (= (subs path 0 1) separator)
      (subs path 1)

      (empty? relative-root)
      path

      :else
      (str relative-root separator path))))

(defn fingerprint-asset [asset-path {:keys [input-root file-hashes]}]
  (let [full-path   (asset-full-path (str asset-path) input-root)
        fingerprint (get file-hashes full-path)]
    (if fingerprint
      (str asset-path "?v=" fingerprint)
      asset-path)))

(defn- last-index [s]
  (dec (count s)))

(defn- drop-last-char [s]
  (subs s 0 (last-index s)))

(defn- drop-trailing-slash [path]
  (if (= (get path (last-index path)) \/)
    (drop-last-char path)
    path))

(defn- absolutize-path [path]
  (let [separator (java.io.File/separator)]
    (if (= (subs path 0 1) separator)
      path
      (str "/" path))))

(defn prepend-asset-host [asset-path asset-host]
  (str (drop-trailing-slash asset-host) (absolutize-path asset-path)))

(defn lookup-fn [{:keys [fingerprint? asset-host] :as opts}]
  (fn [[_ asset-path]]
    (cond-> asset-path
      fingerprint? (fingerprint-asset opts)
      asset-host   (prepend-asset-host asset-host))))

(defn fingerprint
  [{:keys [input-path output-path] :as opts}]
  (let [out (io/file output-path)]
    (io/make-parents out)
    (as-> (slurp input-path) $
      (str/replace $ #"\$\{(.+?)\}" (lookup-fn opts))
      (spit out $))))
