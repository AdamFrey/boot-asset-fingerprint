(set-env!
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.8.0"  :scope "provided"]
                  [boot/core           "2.1.0"  :scope "provided"]
                  [adzerk/bootlaces    "0.1.13" :scope "test"]
                  [metosin/boot-alt-test "0.2.1" :scope "test"]])

(require
  '[adzerk.bootlaces :as deploy]
  '[metosin.boot-alt-test :as boot-test])

(def +version+ "1.1.0")

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

(ns-unmap 'boot.user 'test)

(deftask test []
  (merge-env! :source-paths ["test"])
  (boot-test/alt-test))

(deftask push-release []
  (comp
    (deploy/build-jar)
    (#'deploy/collect-clojars-credentials)
    (push
      :tag            true
      :gpg-sign       false
      :ensure-release true
      :repo           "deploy-clojars")))
