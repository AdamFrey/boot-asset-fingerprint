(set-env!
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                  [adzerk/bootlaces "0.1.13"   :scope "test"]])

(require
  '[adzerk.bootlaces :refer :all]) ;; tasks: build-jar push-snapshot push-release

(def +version+ "1.0.0")

(bootlaces! +version+)

(task-options!
  pom {:project     'afrey/boot-asset-fingerprint
       :version     +version+
       :description "Boot task to fingerprint asset references in html files."
       :url         "https://github.com/AdamFrey/boot-asset-fingerprint"
       :scm         {:url "https://github.com/AdamFrey/boot-asset-fingerprint"}
       :license     {"MIT" "https://opensource.org/licenses/MIT"}})

(deftask dev []
  (comp
    (watch)
    (build-jar)))
