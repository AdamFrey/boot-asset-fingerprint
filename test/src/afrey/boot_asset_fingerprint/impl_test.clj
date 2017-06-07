(ns afrey.boot-asset-fingerprint.impl-test
  (:require [clojure.test :refer :all]
            [afrey.boot-asset-fingerprint.impl :refer :all]
            [boot.core :as boot]
            [clojure.java.io :as io]))

(def test-index-file "test/resources/index.html")

#_(deftest fingerprinted-asset-paths-test
  (let [files [(io/file "test/resources/index.html")
               (io/file "test/resources/other.html")]]
    (is (= (fingerprinted-asset-paths files)
           #{"test-1.js" "/test-2.css" "test-1.css"}))))

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
           "parent/foo.txt"))

    (is (= (asset-full-path "foo.txt" "parent/")
           "parent/foo.txt"))))

(deftest test-fingerprint-asset
  (is (= (fingerprint-asset "/foo.txt" {:file-hashes {}})
         "/foo.txt"))
  (is (= (fingerprint-asset "/foo.txt" {:file-hashes {"foo.txt" "/foo-barbaz.txt"}})
         "/foo-barbaz.txt")))

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

(deftest test-fingerprint
  (testing "absolutizes path without fingerprint when no corresponding hash"
    (is (= "/style.css" (update-asset-references "${style.css}" {}))))
  (testing "replaces the template with the fingerprinted file"
    (is (= "/style-123.css" (update-asset-references "${style.css}" {:file-hashes {"style.css" "style-123.css"}})))))
