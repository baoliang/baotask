(ns baotask.main
    (:require
      [baotask.storage :as st]
      [clojure.java.jdbc :as jdbc]
      [baotask.service :as service]
      [taoensso.timbre :as timbre])
    (:gen-class))


(defn -main [& args]
  )