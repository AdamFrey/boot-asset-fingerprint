(set-env!
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.8.0" :scope "provided"]
                  [adzerk/bootlaces "0.1.13"   :scope "test"]])

(require
  '[adzerk.bootlaces :as deploy])

(def +version+ "1.1.0-SNAPSHOT")

(deploy/bootlaces! +version+)

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
    (deploy/build-jar)))

(deftask push-release []
  (comp
    (deploy/build-jar)
    (#'deploy/collect-clojars-credentials)
    (push
      :tag            true
      :gpg-sign       false
      :ensure-release true
      :repo           "deploy-clojars")))
