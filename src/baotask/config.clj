(ns baotask.config
  (:require [baotask.utils :as utils]))

(def config utils/config)

(def db-sub 
  {:subprotocol  (get-in config ["db" "subprotocol"])
   :subname  (get-in config ["db" "db-name"])
   :user (get-in config ["db" "user"])
   :password (get-in config ["db" "password"])})