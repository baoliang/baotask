(ns baotask.storage
  (:require [taoensso.timbre :as timbre]
            [honeysql.core :as sql]
            [baotask.utils :as utils]
            [baotask.service :as service]
            [baotask.bl.db :as db]
            [baotask.utils :as utils]))


(defn query
  ([database sqlmap]
  (db/query database sqlmap))
  ([sqlmap]
    (query  @service/db-pool sqlmap)))

(defn page [sqlmap-or-tablename page & [pagesize]]
  (query (assoc (db/make-sure-select- sqlmap-or-tablename)
                :order-by [[:id :desc] ]
                :page [page pagesize])))



(defn get-by-id [sqlmap-or-tablename id]
  (first (let [{:keys [select] :as sqlmap} (db/make-sure-select- sqlmap-or-tablename)
               sqlmap (merge {:select select} (dissoc sqlmap :select))]
           (query (assoc sqlmap :where [:= :id id])))))

(defn get-by-colum-value [sqlmap-or-tablename colum value]
  (first (let [{:keys [select] :as sqlmap} (db/make-sure-select- sqlmap-or-tablename)
               sqlmap (merge {:select select} (dissoc sqlmap :select))]
           (query (assoc sqlmap :where [:= colum value])))))

(defn list-by-colum-value [sqlmap-or-tablename colum value]
  (let [{:keys [select] :as sqlmap} (db/make-sure-select- sqlmap-or-tablename)]
    (query (assoc sqlmap :where [:= colum value]))))


(defn list-by-where
  [table sqlmap-query]
  (query {:select [:*]
          :from [table]
          :where sqlmap-query}))

(defn list-count-query [table sqlmap-query]
  (query {:select-scala [:%count.*]
                  :from [table]
                  :where sqlmap-query}))

(defn insert
  ([database tablename value]
    (query database {:insert-into tablename :values (if (map? value)
                                                      [value]
                                                      (if vector?
                                                        value
                                                        (vec value)))}))
  ([tablename value]
    (insert @service/db-pool tablename value)))


(defn create [tablename value]
  (let [id (query {:insert-into tablename :values [value]})]
    (if id (get-by-id tablename id))))

(defn update-by-id
  ([database tablename id value]
   (query database {:update tablename
            :set value
            :where [:= :id id]}))
  ([tablename id value]
   (update-by-id  @service/db-pool tablename id value)))


(defn update-table
  ([database tablename query-where value]
    (query database {:update tablename
            :set value
            :where query-where}))
  ([tablename query-where value]
    (query {:update tablename
            :set value
            :where query-where})))

(defn kv-get
  "and value->>'value' = ?"
  [scope key & [value]]
  (let [data
        (query {:select-row [:*]
                :from [:kvs]
                :where [:and [:= :key key] [:= :scope scope] [:= :del false]]})]
    (if data
      (merge data (utils/from-pg-json (:data data)))
      nil)))

(defn kv-update  "更新kv数据库"
  [scope key  value]
  (let [data (utils/from-pg-json (:data (kv-get scope key )))
        _value (utils/map-key-to-keyword value)
        _ (println (merge data value))
        _data (utils/to-pg-json (merge (utils/map-key-to-keyword data) _value))]

    (query {:update :kvs
            :set {:data _data :update_time (sql/call :now)}
            :where [:and [:= :scope scope] [:= :key key]]})))

(defn kv-del  "删除kv数据库"
  [scope key  ]
  (query {:update :kvs
          :set {:del true :update_time (sql/call :now)}
          :where [:and [:= :scope scope] [:= :key key]]}))

(defn kv-insert  ""
  ([scope key  value]
   (insert :kvs  {:scope scope
                  :key key
                  :data (utils/to-pg-json value)}))
  ([scope key  value company-id]
   (insert :kvs  {:scope scope
                  :key key
                  :data (utils/to-pg-json value)
                  :company-id company-id})))


(defn get-data-by-page
  "根据表名获取分页数据"
  ([table page-number pagesize query]
   (page  (merge (if query {:where query}) {:select [:*]
                                               :from [table]
                                               })
             page-number
             pagesize))
  ([table page-number ]
   (get-data-by-page table page-number 10 nil))
  ([table page-number  pagesize ]
   (get-data-by-page table page-number pagesize nil)))