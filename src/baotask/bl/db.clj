(ns baotask.bl.db
  (:require [taoensso.timbre :as timbre]
            [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]
            [honeysql.format :as fmt]
            [honeysql.helpers :as helper]
            [baotask.config :refer [config]]
            [baotask.service :as service]
            [baotask.utils :as utils]))

(defn log [query]
  (timbre/info query)
  query)

(defn format-and-log [sqlmap]
  (if (=  "mysql"  (get-in config ["db" "subprotocol"]))
    ;(log (clojure.string/replace  #"NULL" ""))
    (log (sql/format sqlmap))
    (log (sql/format sqlmap :quoting :ansi))))

(defmulti query
          (fn [db sqlmap]
            (if (string? sqlmap)
              :default
              (cond (some sqlmap [:update :update-ignore :delete-from]) :execute
                    (some sqlmap [:insert-into :insert-ignore]) :insert
                    (some sqlmap [:page]) :page
                    (some sqlmap [:select]) :select
                    (some sqlmap [:select-row]) :select-row
                    (some sqlmap [:select-scala]) :select-scala))))

(defmethod query :default [db sqlmap]
  (jdbc/query db sqlmap :identifiers str))

(defmethod query :execute [db sqlmap]
  (first (jdbc/execute! db (format-and-log sqlmap))))

(defmethod query :insert [db sqlmap]
  (let [[sql & params] (format-and-log sqlmap)
        {r :generated_key} (jdbc/db-do-prepared-return-keys db sql params)]
    r))

(defmethod query :select [db sqlmap]
  (jdbc/query db (format-and-log sqlmap) :identifiers str))

(defmethod query :select-row [db {select :select-row :as sqlmap}]
  (first (jdbc/query db (format-and-log (assoc (dissoc sqlmap :select-row)
                                          :select select
                                          :limit 1)))))

(defmethod query :select-scala [db {select :select-scala :as sqlmap}]
  (first (vals (first (jdbc/query db (format-and-log (assoc (dissoc sqlmap :select-scala)
                                                       :select select
                                                       :limit 1)))))))

(defn- calculate-page [[page pagesize]]
  (let [page (utils/parse-int page 1)
        pagesize (utils/parse-int pagesize 10)
        page (if (> page 1) page 1)
        pagesize (if (> pagesize 0) pagesize 10)]
    [page pagesize]))

(defmethod query :page [db sqlmap]
  (let [cnt (query db (merge {:select-scala [:%count.*]}
                             (dissoc sqlmap :page :select :order-by :limit :offset)))
        [page pagesize] (calculate-page (:page sqlmap))
        result {:count cnt :list [] :page page :pagesize pagesize :pagecount (int (if (> cnt 0) (Math/ceil (/ (double cnt) pagesize)) 0))}]
    (if (> cnt 0)
      (assoc result
        :list (jdbc/query db (format-and-log (assoc (dissoc sqlmap :select-scala :page)
                                               :limit pagesize :offset (* pagesize (- page 1)))) :identifiers str))
      result)))

(defn make-sure-select- [sqlmap-or-tablename]
  (if (keyword? sqlmap-or-tablename)
    {:select [:*] :from [sqlmap-or-tablename]}
    (merge {:select [:*]} sqlmap-or-tablename)))

(defn rollback []
  (timbre/info "transaction rollback")
  (jdbc/db-set-rollback-only! @service/db-pool))

(defn cols-plus [columns amount]
  (into {} (for [col columns] [col (sql/call :+ col amount)])))

(defn cols-minus [columns amount]
  (into {} (for [col columns] [col (sql/call :- col amount)])))

(defn sql-now [] (sql/call :now))





