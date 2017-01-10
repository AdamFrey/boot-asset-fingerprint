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

(deftest test-fingerprint-asset
  (is (= (fingerprint-asset "/foo.txt" {:file-hashes {}})
         "/foo.txt"))
  (is (= (fingerprint-asset "/foo.txt" {:file-hashes {"foo.txt" "barbaz"}})
         "/foo.txt?v=barbaz")))

(deftest test-prepend-asset-host
  (testing "skip when no asset-host is given"
    (is (= (prepend-asset-host "/foo.txt" nil)
           "/foo.txt"))

    (is (= (prepend-asset-host "/foo.txt" "")
           "/foo.txt")))

  (testing "prepend the asset host with only one separating slash"
    (is (= (prepend-asset-host "/foo.txt" "assets.example.org")
           "assets.example.org/foo.txt")))

  (testing "drops a possible trailing slash on the asset host"
    (is (= (prepend-asset-host "/foo.txt" "assets.example.org/")
           "assets.example.org/foo.txt"))))
