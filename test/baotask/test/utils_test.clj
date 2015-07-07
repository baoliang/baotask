(ns baotask.test.utils-test
  (:use clojure.test)
  (:require
    [baotask.utils :as utils]))

(deftest test-utils
  (testing "get interest"
    (println (utils/get-current-day-sql-time-start))
    (= "" (utils/get-current-day-sql-time-start))))
