(set-env!
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [afrey/boot-asset-fingerprint "1.0.0-SNAPSHOT"]
                  [pandeiro/boot-http "0.7.3" :scope "test"]])

(require
  '[afrey.boot-asset-fingerprint :refer [asset-fingerprint]]
  '[pandeiro.boot-http :refer [serve]])

(deftask dev []
  (comp
    (watch)
    (serve :port 5000 :reload true)
    (asset-fingerprint)
    (target)))
