(ns baotask.db-spec
  (:require [baotask.storage :as st]
    [clojure.java.jdbc :as jdbc]
    [baotask.service :as service]
            [speclj.core :refer :all]))




(st/with-db-trans
  (st/update-table :users [:= :username "333"] {:email "zzzzzzz"})
  (println "start " @service/db-pool "end")
  (jdbc/db-set-rollback-only!  @service/db-pool)
  )
