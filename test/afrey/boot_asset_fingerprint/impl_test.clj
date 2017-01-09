(ns afrey.boot-asset-fingerprint.impl-test
  (:require [clojure.test :refer :all]
            [afrey.boot-asset-fingerprint.impl :refer :all]))

(deftest test-asset-full-path
  (testing "absolute paths"
    (is (= (asset-full-path "/foo.txt" "")
           "foo.txt"))
    (is (= (asset-full-path "/foo.txt" "parent")
           "foo.txt")))

  (testing "relative paths"
    (is (= (asset-full-path "foo.txt" "")
           "foo.txt"))
    (is (= (asset-full-path "foo.txt" "parent")
           "parent/foo.txt"))))
