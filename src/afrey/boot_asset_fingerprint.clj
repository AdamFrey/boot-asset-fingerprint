(ns afrey.boot-asset-fingerprint
  {:boot/export-tasks true}
  (:require [boot.core :as core]
            [boot.util :as util]
            [clojure.java.io :as io]
            [afrey.boot-asset-fingerprint.impl :as impl]))

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
   e extensions EXT  [str] "Add a file extension to indicate the files to process for asset references."
   _ asset-host HOST str   "Host to prefix all asset urls with"]
  (let [prev       (atom nil)
        output-dir (core/tmp-dir!)]
    (core/with-pre-wrap fileset
      (let [sources     (->> fileset
                          (core/fileset-diff @prev)
                          (core/output-files)
                          (core/by-ext (or extensions [".html"])))
            file-hashes (into {}
                          (map (juxt :path :hash))
                          (core/output-files fileset))]
        (reset! prev fileset)

        (when (seq sources)
          (util/info "Fingerprinting...\n")
          (doseq [file sources
                  :let [path        (:path file)
                        input-path  (-> file core/tmp-file .getPath)
                        output-path (-> (io/file output-dir path) .getPath)
                        input-root  (path->parent-path path)
                        out-file    (io/file output-path)]]
            (do
              (io/make-parents out-file)
              (as-> (slurp input-path) $
                (impl/fingerprint $ {:input-root   input-root
                                     :fingerprint? (not skip)
                                     :asset-host   asset-host
                                     :file-hashes  file-hashes})
                (spit out-file $)))))

        (-> fileset
          (core/add-resource output-dir)
          (core/commit!))))))
