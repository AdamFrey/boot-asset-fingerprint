(set-env!
  :resource-paths #{"resources"}
  :dependencies '[[org.clojure/clojure "1.8.0"]
                  [afrey/boot-asset-fingerprint "1.0.0-SNAPSHOT"]])

(require '[afrey.boot-asset-fingerprint :refer [asset-fingerprint]])

(deftask dev []
  (comp
    (asset-fingerprint)
    (target)))
