(ns baotask.db
  (:import com.mchange.v2.c3p0.ComboPooledDataSource)
  (:require [baotask.utils :as utils]
            [clojure.java.jdbc :as jdbc]
            [taoensso.timbre :as timbre]
            [honeysql.core :as sql]
            [honeysql.format :as fmt]
            [honeysql.helpers :as helper]
            [taoensso.carmine :as carmine]))

(defn calculate-page [[page pagesize]]
  (let [page (utils/parse-int page 1)
        pagesize (utils/parse-int pagesize 10)
        page (if (> page 1) page 1)
        pagesize (if (> pagesize 0) pagesize 10)]
    [page pagesize]))

(helper/defhelper select-row [m fields]
                  (assoc m :select-row (helper/collify fields)))

(defmethod fmt/format-clause :select-row [[op v] sqlmap]
  (let [m (assoc sqlmap
            :select v
            :limit 1)
        m (dissoc m :select-row)]
    (fmt/format-clause (first {:select v}) m)))

(helper/defhelper select-one [m fields]
                  (assoc m :select-one (helper/collify fields)))

(defmethod fmt/format-clause :select-one [[op v] sqlmap]
  (let [m (assoc sqlmap
            :select v
            :limit 1)
        m (dissoc m :select-row)]
    (fmt/format-clause (first {:select v}) m)))

(helper/defhelper page [m fields]
                  (assoc m :page (helper/collify fields)))

(defmethod fmt/format-clause :page [[op v] sqlmap]
  (let [[page pagesize] (calculate-page v)
        o (str "LIMIT " (fmt/to-sql pagesize))]
    (if (> page 1)
      (str o " OFFSET " (fmt/to-sql (* pagesize (- page 1))))
      o)))

(defmethod helper/build-clause :insert-ignore [_ m table]
  (assoc m :insert-ignore table))

(defmethod fmt/format-clause :insert-ignore [[_ table] _]
  (str "INSERT IGNORE INTO " (fmt/to-sql table)))

(defmethod helper/build-clause :insert-select [_ m table]
  (assoc m :insert-select table))

(defmethod fmt/format-clause :insert-select [[_ table] _]
  (str "INSERT INTO " (fmt/to-sql table)))

(helper/defhelper insert-select-columns [m fields]
                  (assoc m :insert-select-columns (helper/collify fields)))

(defmethod fmt/format-clause :insert-select-columns [[_ fields] _]
  (str "(" (fmt/comma-join (map fmt/to-sql fields)) ")"))

(defn insert-ignore
  ([table] (insert-ignore nil table))
  ([m table] (helper/build-clause :insert-ignore m table)))

(comment (def ^:const +clause-order+ (vec (concat [:insert-select :insert-select-columns :select-row :select-one :insert-ignore] fmt/clause-order [:page]))))
(def ^:const +clause-order+ [:insert-select :insert-select-columns :select-row :select-one :insert-ignore
                             ;;fmt/clause-order
                             :select :insert-into :update :delete-from :columns :set :from :join
                             :left-join :right-join :where :group-by :having :order-by :limit :offset
                             :values :query-values
                             ;;fmt/clause-order end
                             :page])
(def ^:const +known-clauses+ (set +clause-order+))

(defn- to-sql- [query]
  (let [sql (sql/format query)]
    (timbre/info sql)
    sql))

(defmulti query- (fn [db query]
                   (cond (some query [:update :delete-from]) :execute
                         (some query [:insert-select :insert-into :insert-ignore]) :insert
                         (some query [:page]) :page
                         (some query [:select]) :select
                         (some query [:select-row]) :select-row
                         (some query [:select-one]) :select-one)))

(defmethod query- :default [db query]
  (println query))

(defmethod query- :execute [db query]
  (first (jdbc/execute! db (to-sql- query))))

(defmethod query- :insert [db query]
  (let [[sql & params] (to-sql- query)
        {r :generated_key} (jdbc/db-do-prepared-return-keys db sql params)]
    (or r true)))

(defmethod query- :select [db query]
  (jdbc/query db (to-sql- query)))

(defmethod query- :select-row [db query]
  (let [{:keys [select-row]} query
        query (assoc query :limit 1)]
    (first (jdbc/query db (to-sql- query)))
    ))

(defmethod query- :select-one [db query]
  (let [{:keys [select-one]} query
        query (assoc query :limit 1)]
    (-> (jdbc/query db (to-sql- query))
        first
        vals
        first)))

(defmethod query- :page [db query]
  (let [cnt-query (dissoc query :page :select :order-by :limit :offset)
        cnt-query (if (:select-one cnt-query) cnt-query (select-row cnt-query :%count.*))
        cnt (-> (jdbc/query db (to-sql- cnt-query))
                first
                vals
                first)
        r {:count cnt :list nil}]
    (if (> cnt 0)
      (let [list-query (dissoc query :select-one)
            [page pagesize] (calculate-page (:page list-query))
            list (jdbc/query db (to-sql- list-query))
            rem-page (rem  cnt pagesize)]
        (assoc r :list list :page page :pagesize pagesize :pagecount (+ (int (/ cnt pagesize)) (if (= 0 rem-page) 0 1))))
      (let [[page pagesize] (calculate-page (:page query))]
        (assoc r
          :page page
          :pagesize pagesize)))))

(defmacro defquery [name db]
  `(defn ~name [~'query] (query- ~db ~'query)))

(defmacro defquerybuild [name db]
  `(defn ~name [& ~'clauses] (query- ~db (apply sql/build ~'clauses))))


(defn pool-c3p0
  [config]
  (let [class-name
        (if (= "mysql" (get-in config ["db" "subprotocol"]))
          "com.mysql.jdbc.Driver"
          "org.postgresql.Driver")
        cpds (doto (ComboPooledDataSource.)
               (.setDriverClass class-name)
               (.setJdbcUrl (str "jdbc:" (get-in config ["db"  "subprotocol"]) ":" (str "//" (get-in config ["db"  "host"]) ":" (get-in config ["db"  "port"]) "/" (get-in config ["db" "db-name"]) (if (= "mysql" (get-in config ["db" "subprotocol"])) "?useUnicode=true&characterEncoding=UTF-8" ""))))
               (.setUser (get-in config ["db" "user"]))
               (.setPassword (get-in config ["db" "password"]))
               (.setAutoCommitOnClose false)

               ;; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ;; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))


(def db-pool (atom nil))
(defquery query @db-pool)






