(ns afrey.boot-asset-fingerprint
  {:boot/export-tasks true}
  (:require [boot.core :as core]
            [boot.util :as util]
            [clojure.java.io :as io]
            [afrey.boot-asset-fingerprint.impl :as impl]))

(defn path->parent-path [path]
  (or (-> path io/file .getParent) ""))

(defn fingerprint-file-path [{:keys [path hash]}]
  (let [regex       #"(.*)\.([^.]*?)$"
        replacement (clojure.string/replace path regex (str "$1-" hash ".$2"))]
    [path replacement]))

(def default-source-extensions [".html"])

(defn assets-to-fingerprint
  "Given a fileset and a set of source files, return a seq of all files
  referenced in the source files."
  [fileset source-files asset-root]
  (when (seq source-files)
    (let [assets-to-fingerprint (impl/fingerprinted-asset-paths source-files asset-root)]
      (->> fileset
        (core/output-files)
        (filter #(contains? assets-to-fingerprint (:path %)))))))

(defn assets->file-rename-map [files]
  (into {}
    (map fingerprint-file-path)
    files))

(defn fingerprint-asset-files! [{:keys [assets file-hashes output-dir verbose?]}]
  (doseq [file assets
          :let [file-path (:path file)
                out-file-path (get file-hashes file-path)
                out-file (io/file output-dir out-file-path)]]
    (do
      (when verbose?
        (util/info (str "Renaming file " file-path " to " out-file-path "\n")))
      (io/make-parents out-file)
      (io/copy (core/tmp-file file) out-file))))

;; We have two tasks.
;;
;; 1) to rename asset files to add a fingerprint to the filename based on the
;; file's contents
;;
;; 2) to modify any references to those files from the bare filenames to the
;; fingerprinted versions.
;;
;; The file fingerprints many not match up perfectly with the final file's
;; contents because a file may have modified references within it that have
;; changed.

(core/deftask asset-fingerprint
  "Replace asset references with a URL query-parameter based on the hash contents.

  The main purpose of doing this is for cache-busting static assets
  that were deployed with a far-future expiration date. See the Ruby
  on Rails Asset Pipeline guide, segment \"What is Fingerprinting and
  Why Should I Care\" for a detailed explanation of why you want to do this.
  (http://guides.rubyonrails.org/asset_pipeline.html#what-is-fingerprinting-and-why-should-i-care-questionmark) "
  [s skip                    bool  "Skips file fingerprinting and replaces each asset url with bare"
   e extensions        EXT   [str] "Add a file extension to indicate the files to process for asset references."
   _ asset-host        HOST  str   "Host to prefix all asset urls with e.g. https://your-host.com"
   a asset-root        ROOT  str   "The root dir where the assets are served from"
   v verbose                 bool  "Run this task with verbose logging enabled"]
  (let [prev       (atom nil)
        output-dir (core/tmp-dir!)
        skip?      (boolean skip)
        verbose?   (boolean verbose)]
    (core/with-pre-wrap fileset
      (let [sources         (->> fileset
                              #_(core/fileset-diff @prev) ; Diff against previous fileset
                              (core/input-files)
                              (core/by-ext (or extensions default-source-extensions)))
            sources-paths   (into #{} (map :path) sources)
            assets          (if skip?
                              (do
                                (when verbose? (util/info "Skipping asset fingerprinting\n"))
                                [])
                              (assets-to-fingerprint fileset sources asset-root))
            file-rename-map (assets->file-rename-map assets)]
        (reset! prev fileset)

        (when (seq assets)
          (util/info "Asset Fingerprinting...\n")
          (fingerprint-asset-files! {:assets      assets
                                     :file-hashes file-rename-map
                                     :output-dir  output-dir
                                     :verbose?    verbose?}))

        (doseq [file sources
                :let [original-path (:path file)
                      path          (get file-rename-map original-path original-path)
                      input-path    (-> file core/tmp-file .getPath)
                      output-path   (-> (io/file output-dir path) .getPath)
                      out-file      (io/file output-path)]]
          (do
            (when verbose?
              (util/info (str "Updating content in source file " path "\n")))
            (io/make-parents out-file)
            (let [new-file-content (-> (slurp input-path)
                                       (impl/update-asset-references
                                        {:asset-root  asset-root
                                         :skip?       skip?
                                         :asset-host  asset-host
                                         :file-hashes file-rename-map
                                         :verbose?    verbose?}))]
              (spit out-file new-file-content))))

        (-> fileset
          (core/add-resource output-dir)
          (core/rm assets) ; Remove files that have been moved
          (core/commit!))))))
