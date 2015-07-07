(ns baotask.test.storage-test
    (:use clojure.test)
    (:require
      [baotask.storage :as storage]))

(deftest test-storage
         (testing "get interest"
                  (is (= nil (storage/kv-update "sources" "4edda4c9-5b4b-42b5-b85e-90481ddc23ed" {"name" "xx"})))
                  (is (= 0 (storage/list-count-query :kvs [:= :id 0])))))
