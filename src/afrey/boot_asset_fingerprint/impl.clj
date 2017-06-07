(ns afrey.boot-asset-fingerprint.impl
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [boot.core :as boot]
            [boot.util :as util]))

(def selector-regex #"\$\{(.+?)\}")

(defn file-parent
  [file-path]
  (re-find #"^.+\/" file-path))

(defn- remove-leading-slash [s]
  (when s
    (str/replace s (re-pattern (str "^" (java.io.File/separator))) "")))

(defn- remove-trailing-slash [s]
  (when s
    (str/replace s (re-pattern (str (java.io.File/separator) "$")) "")))

(defn- remove-asset-root [path asset-root]
  (cond-> path
    (not-empty asset-root)
    (str/replace-first (re-pattern (str asset-root (java.io.File/separator)))
                       "")))

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
      (str (remove-trailing-slash relative-root) separator path))))

(defn fingerprinted-asset-paths
  "Given a fileset returns a set of string paths to all desired
  assets that should be fingerprinted."
  [fileset asset-root]
  (into #{}
        (comp
         (map (fn [file]
                (let [file-text (slurp (boot/tmp-file file))]
                  (map (fn [[_ asset-path]]
                         (asset-full-path asset-path asset-root))
                       (re-seq selector-regex file-text)))))
         cat)
        fileset))

(defn fingerprint-asset [asset-path {:keys [asset-root file-hashes verbose?]}]
  (let [full-path (asset-full-path (str asset-path) asset-root)
        fingerprinted-path (-> (get file-hashes full-path asset-path)
                               (remove-asset-root asset-root))]
    (when verbose?
      (util/info (str "\tRenaming reference " asset-path " to " fingerprinted-path "\n")))
    fingerprinted-path))

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
  (str (drop-trailing-slash asset-host) asset-path))

(defn lookup-fn [{:keys [asset-host] :as opts}]
  (fn [[_ asset-path]]
    (if (:skip? opts)
      asset-path

      (cond-> asset-path
        :always    (fingerprint-asset opts)
        :always    (absolutize-path)
        asset-host (prepend-asset-host asset-host)))))

(defn update-asset-references
  [text opts]
  (str/replace text selector-regex (lookup-fn opts)))
