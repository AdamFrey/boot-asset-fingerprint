(set-env!
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [afrey/boot-asset-fingerprint "1.1.0"]
                  [tailrecursion/boot-jetty "0.1.3" :scope "test"]])

(require
  '[afrey.boot-asset-fingerprint :refer [asset-fingerprint]]
  '[tailrecursion.boot-jetty :as jetty])

(deftask dev []
  (comp
    (watch)
    (asset-fingerprint :extension [".css" ".html"])
    (jetty/serve :port 5000)
    (target)))
