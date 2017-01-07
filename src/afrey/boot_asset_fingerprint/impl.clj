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
    (if (= (subs path 0 1) separator)
      ;; absolute path
      (subs path 1)
      ;; relative path
      (if (empty? relative-root)
        path
        (str relative-root separator path)))))

(defn fingerprint-asset [asset-path fingerprint]
  (if fingerprint
    (str asset-path "?v=" fingerprint)
    asset-path))

(defn lookup-fn [{:keys [skip file-hashes input-root]}]
  (fn [[_ asset-path]]
    (if skip
      asset-path

      (let [full-path (asset-full-path (str asset-path) input-root)
            fingerprint (get file-hashes full-path)]
        (fingerprint-asset asset-path fingerprint)))))

(defn fingerprint
  [{:keys [input-path output-path] :as opts}]
  (let [out (io/file output-path)]
    (io/make-parents out)
    (spit out (str/replace (slurp input-path) #"\$\{(.+?)\}" (lookup-fn opts)))))
