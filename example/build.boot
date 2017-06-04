(set-env!
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [afrey/boot-asset-fingerprint "1.3.1"]
                  [tailrecursion/boot-jetty "0.1.3" :scope "test"]])

(require
  '[afrey.boot-asset-fingerprint :refer [asset-fingerprint]]
  '[tailrecursion.boot-jetty :as jetty])

(deftask dev
  [s skip bool]
  (comp
    (watch)
    (asset-fingerprint :extensions [".css" ".html"] :skip skip)
    (jetty/serve :port 5000)
    (target)))
