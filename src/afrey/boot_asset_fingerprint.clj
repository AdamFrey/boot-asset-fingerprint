(ns afrey.boot-asset-fingerprint
  {:boot/export-tasks true}
  (:require [boot.core :as core]
            [boot.util :as util]
            [clojure.java.io :as io]
            [afrey.boot-asset-fingerprint.impl :as impl]))

(defn- filter-by-extns [fileset extensions]
  (->> fileset
    (core/output-files)
    (core/by-ext extensions)))

(defn path->parent-path [path]
  (or (-> path io/file .getParent) ""))

(core/deftask asset-fingerprint
  "Replace asset references with a URL query-parameter based on the hash contents.

  The main purpose of doing this is for cache-busting static assets
  that were deployed with a far-future expiration date. See the Ruby
  on Rails Asset Pipeline guide, segment \"What is Fingerprinting and
  Why Should I Care\" for a detailed explanation of why you want to do this.
  (http://guides.rubyonrails.org/asset_pipeline.html#what-is-fingerprinting-and-why-should-i-care-questionmark) "
  [s skip            bool  "Skips file fingerprinting and replaces each asset url with bare"
   e extensions  EXT  [str] "Add a file extension to indicate the files to process for asset references."
   _ asset-host HOST str   "Host to prefix all asset urls with"
   ]
  (let [prev        (atom nil)
        tmp-dir     (core/tmp-dir!)
        extensions (or extensions [".html"])]
    (core/with-pre-wrap fileset
      (core/empty-dir! tmp-dir)
      (let [diff        (core/fileset-diff @prev fileset)
            files       (filter-by-extns diff extensions)
            file-hashes (into {}
                          (map (juxt :path :hash))
                          (core/output-files fileset))]
        (reset! prev fileset)
        (doseq [file files
                :let [path (:path file)
                      input-path (-> file core/tmp-file .getPath)
                      output-path (-> (io/file tmp-dir path) .getPath)
                      input-root (path->parent-path path)]]
          (do
            (util/info "Fingerprinting %s...\n" path)
            (impl/fingerprint
             {:input-path  input-path
              :input-root  input-root
              :output-path output-path
              :fingerprint? (not skip)
              :asset-host asset-host
              :file-hashes file-hashes})))

        (-> fileset (core/add-resource tmp-dir) core/commit!)))))
