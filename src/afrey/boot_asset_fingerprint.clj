(ns afrey.boot-asset-fingerprint
  {:boot/export-tasks true}
  (:require [boot.core :as core]
            [boot.pod :as pod]
            [boot.util :as util]
            [clojure.java.io :as io]))

(defn- pod-deps []
  (remove pod/dependency-loaded? '[[enlive "1.1.6"]]))

(defn- init
  [fresh-pod]
  (pod/with-eval-in fresh-pod
    (require '[afrey.boot-asset-fingerprint.impl])))

(defn- fileset->html-files [fileset]
  (->> fileset
    (core/output-files)
    (core/by-ext [".html"])))

(defn path->parent-path [path]
  (or (-> path io/file .getParent) ""))

(core/deftask asset-fingerprint
  "Fingerprint files in a pod"
  [s skip bool "Skips file fingerprinting and replaces each asset url with bare TODO"]

  (let [updated-env (update (core/get-env) :dependencies into (pod-deps))
        pods (pod/pod-pool updated-env :init init)]
    (core/cleanup (pods :shutdown))
    (core/with-pre-wrap fileset
      (let [worker-pod (pods :refresh)
            tmp-dir (core/tmp-dir!)
            html-files (fileset->html-files fileset)
            file-hashes (into {}
                          (map (juxt :path :hash))
                          (core/output-files fileset))]

        (doseq [file html-files
                :let [path (:path file)
                      input-path (-> file core/tmp-file .getPath)
                      output-path (-> (io/file tmp-dir path) .getPath)
                      input-root (path->parent-path path)]]
          (do
            (util/info (format "Fingerprinting %s...\n" path))
            (pod/with-eval-in worker-pod
              (afrey.boot-asset-fingerprint.impl/fingerprint
                ~{:input-path input-path
                  :input-root input-root
                  :output-path output-path
                  :skip skip
                  :file-hashes file-hashes}))))

        (-> fileset (core/add-resource tmp-dir) core/commit!)))))
