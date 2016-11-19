(set-env!
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [afrey/boot-asset-fingerprint "1.0.0-SNAPSHOT"]
                  [tailrecursion/boot-jetty "0.1.3" :scope "test"]])

(require
  '[afrey.boot-asset-fingerprint :refer [asset-fingerprint]]
  '[tailrecursion.boot-jetty :as jetty]
  '[clojure.java.io :as io])

(deftask dev []
  (comp
    (watch)
    (asset-fingerprint)
    (jetty/serve :port 5000)
    (target)))
