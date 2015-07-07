(ns baotask.main
    (:require
      [baotask.storage :as st]
      [clojure.java.jdbc :as jdbc]
      [baotask.service :as service]
      [taoensso.timbre :as timbre])
    (:gen-class))


(defn -main [& args]
    (try
      (jdbc/with-db-transaction
       [db @service/db-pool]
        (st/update-table db :users [:= :username "333"] {:email "ccc"})
       (st/update-table db :userszzz [:= :username "333"] {:email "zzzz"})
        )
      (catch Exception e
        (timbre/info "error" e)
        )))