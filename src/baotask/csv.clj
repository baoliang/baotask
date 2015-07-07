(ns baotask.csv
  (:require [clojure-csv.core :as clojure-csv]
             [clojure.java.io :as io]))

(defn take-csv
  "Takes file name and reads data."
  [fname]
  (with-open [file (io/reader fname)]
    (doall (map (comp first clojure-csv/parse-csv) (line-seq file)))))