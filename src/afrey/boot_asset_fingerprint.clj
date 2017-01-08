(ns afrey.boot-asset-fingerprint
  {:boot/export-tasks true}
  (:require [boot.core :as core]
            [boot.pod :as pod]
            [boot.util :as util]
            [boot.task.built-in :as built-in]
            [clojure.java.io :as io]))

(defn- pod-init
  [fresh-pod]
  (pod/with-eval-in fresh-pod
    (require '[afrey.boot-asset-fingerprint.impl])))

(defn- fileset->html-files [fileset extensions]
  (->> fileset
    (core/output-files)
    (core/by-ext extensions)))

(defn path->parent-path [path]
  (or (-> path io/file .getParent) ""))

(core/deftask asset-fingerprint
  "Fingerprint files in a pod"
  [s skip            bool  "Skips file fingerprinting and replaces each asset url with bare"
   e extensions  EXT  [str] "Add a file extension to indicate the files to process for asset references."
   _ asset-host HOST str   "Host to prefix all asset urls with"
   ]
  (let [prev        (atom nil)
        pods        (pod/pod-pool (core/get-env) :init pod-init)
        tmp-dir     (core/tmp-dir!)
        extensions (or extensions [".html"])]
    (core/cleanup (pods :shutdown))
    (core/with-pre-wrap fileset
      (core/empty-dir! tmp-dir)
      (let [diff        (core/fileset-diff @prev fileset)
            worker-pod  (pods :refresh)
            html-files  (fileset->html-files diff extensions)
            file-hashes (into {}
                          (map (juxt :path :hash))
                          (core/output-files fileset))]
        (reset! prev fileset)
        (doseq [file html-files
                :let [path (:path file)
                      input-path (-> file core/tmp-file .getPath)
                      output-path (-> (io/file tmp-dir path) .getPath)
                      input-root (path->parent-path path)]]
          (do
            (util/info "Fingerprinting %s...\n" path)
            (pod/with-eval-in worker-pod
              (afrey.boot-asset-fingerprint.impl/fingerprint
                ~{:input-path  input-path
                  :input-root  input-root
                  :output-path output-path
                  :fingerprint? (not skip)
                  :asset-host asset-host
                  :file-hashes file-hashes}))))

        (-> fileset (core/add-resource tmp-dir) core/commit!)))))
