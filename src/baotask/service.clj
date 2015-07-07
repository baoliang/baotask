(ns baotask.service
  (:require [taoensso.carmine :as carmine]
            [baotask.db :refer [pool-c3p0]]
            [baotask.db :refer [defquery defquerybuild]]
            [baotask.config :refer [config] ]))

(def db-pool (atom nil))
(defquery query @db-pool)
(defquerybuild build @db-pool)
(reset! db-pool (pool-c3p0 config))


