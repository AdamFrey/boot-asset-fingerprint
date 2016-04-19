(ns afrey.boot-asset-fingerprint.impl
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]))

(defn asset-full-path
  "Return the full path of an asset, taking into account relative and absolute paths TODO"
  ;; (asset-root "foo.txt" "") => "foo.txt"
  ;; (asset-root "/foo.txt" "") => "foo.txt"
  ;; (asset-root "/foo.txt" "parent") => "foo.txt"
  ;; (asset-root "foo.txt" "parent") => "parent/foo.txt"

  [path relative-root]
  (let [separator (java.io.File/separator)]
    (if (= (subs path 0 1) separator)
      ;; absolute path
      (subs path 1)
      ;; relative path
      (str relative-root separator path))))

(defn fingerprint-asset [asset-path fingerprint]
  (if fingerprint
    (str asset-path "?v=" fingerprint)
    asset-path))

(defn template [input-file {:keys [skip file-hashes input-root]}]
  (html/template (html/html-resource input-file)
    []
    [html/any-node] (html/replace-vars
                      (fn [asset-name]
                        (let [asset-path (subs (str asset-name) 1)]
                          (if skip
                            asset-path

                            (let [full-path (asset-full-path asset-path input-root)
                                  fingerprint (get file-hashes full-path)]
                              (fingerprint-asset asset-path fingerprint))))))))


(defn fingerprint
  [{:keys [input-path output-path] :as opts}]
  (let [out (io/file output-path)]
    (io/make-parents out)
    (spit out (reduce str ((template (io/file input-path) opts))))))
