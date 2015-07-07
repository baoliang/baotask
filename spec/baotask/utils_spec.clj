(ns baotask.utils-spec
  (:require [baotask.utils :as utils]
  			    [speclj.core :refer :all]))



(describe "utils test"

	(before
		(if (.exists
        (clojure.java.io/file "download/testdownload"))
			(clojure.java.io/delete-file
        (clojure.java.io/file "download/testdownload"))))

	(after
		(if (.exists (clojure.java.io/file "download/testdownload"))
			(clojure.java.io/delete-file
        (clojure.java.io/file "download/testdownload"))))

  (it "test get config.clj"
    (should (= "test"
               ((utils/get-config) "test"))))

  (it "test clojure obj to pgobject"
    (should (= "json"
               (.getType (utils/to-pg-json {:1 1})))))

  (it "test pgobject obj to clojure"
      (should (= 1
                 ((utils/from-pg-json (utils/to-pg-json {:test 1})) "test"))))

  (it "tests if .exists returns true"
  	(utils/download "download/testdownload" "http://tp3.sinaimg.cn/1044955110/180/5650086686/1")
    (should (.exists
              (clojure.java.io/file "download/testdownload")))))


(run-specs)