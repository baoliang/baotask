(ns baotask.resque
  (:require [resque-clojure.core :as resque]))

(defn add-job []
  (resque/configure {:host "localhost" :port 6379}) ;; optional
  ;; creating a job
  (resque/enqueue "testqueue" "clojure.core/println" "hello" "resque")
  ;; listening for jobs
  (resque/start ["testqueue"]))