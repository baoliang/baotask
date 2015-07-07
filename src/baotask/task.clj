;(ns baotask.task
;  (:use baotask.api
;    korma.core)
;  (:require [baotask.db :as db]
;            [clojure.data.json :as json]
;            [net.cgrand.enlive-html :as html]
;            [hiccup.core :as hiccup]
;            [baotask.utils :as utils]
;            [baotask.yuese-db :as y-db]))
;(def download-path "./download")
;
;(defn sql-now []
;  (java.sql.Timestamp. (.getTime (java.util.Date.))))
;
;(defn insert-into-db [item]
;  (def item-list (select db/items
;      (where {:num_iid (item "num_iid")})))
;  (if (= 0 (.size item-list))
;    (insert db/items
;      (values [(clojure.walk/keywordize-keys (assoc  item :created_time (sql-now)))]))
;    (update db/items
;      (set-fields (clojure.walk/keywordize-keys (assoc item :created_time (:created_time (first item-list)))))
;      (where {:num_iid (item "num_iid")}))))
;
;(defn get-items-by-page [ keyword cid start-price end-price city]
;  (def results (((get-taobaoke-item 1
;                                    keyword
;                                    cid
;                                    start-price
;                                    end-price
;                                    city)
;                      "taobaoke_items_get_response") "total_results") )
;  (println "results")
;  (println results)
;  ;获取淘宝客商品
;  (doseq [i (range
;              (/ results 40))]
;    (try
;      (let [items (get-taobaoke-item (+ i 1) keyword cid start-price end-price city)]
;        (doseq [item  (((items "taobaoke_items_get_response") "taobaoke_items") "taobaoke_item")]
;          (do
;            (println "price------------")
;            (println item)
;            (insert-into-db (assoc item   :tag keyword :number_price (read-string (item :price)))))))
;    (catch Exception e
;      (println i)))))
;
;
;(defn get-relate-items [num_iid]
;  ;获取淘宝客商品
;    (try
;      (doseq [rel-item  ((((get-taobaoke-relate num_iid) "taobaoke_items_relate_get_response") "taobaoke_items") "taobaoke_item")]
;          (do
;            (insert-into-db (assoc rel-item :tag "" :relateid num_iid :updated_time (sql-now)))))
;    (catch Exception e
;        (println e))))
;
;
;(defn get-tmao-good []
;  ;获取天猫精品
;  (try
;    (doseq []
;      (println "tmall"))
;    (catch Exception e
;      (println e))))
;
;(defn get-pin6-novel []
;  ;获取小说数据
;  (doseq [i (range 129 300)]
;
;    (doseq [a (html/select
;              (try
;                (utils/get-page (str "http://www.p6we.com/thread.php?fid=43&search=&page=" i))
;              (catch Exception e (do (str "caught exception: " (.getMessage e)) ())))
;              [:td.t_one :a])]
;
;      (if-not (and (select y-db/novel (where {:title (html/text a)}))
;                   (re-find (re-matcher #"&page=" (first (html/texts (html/attr-values  a :href)))))
;                   (not (re-find (re-matcher #"read.php" (first (html/texts (html/attr-values  a :href)))))))
;
;        (let [titles (html/text a)
;
;              detail (try
;                        (utils/get-page (str "http://www.p6we.com/" (first (html/texts (html/attr-values  a :href)))))
;                      (catch Exception e (do (println (str "caught exception: " (.getMessage e))) ())))]
;
;
;          (if (and (first (html/texts (html/select  detail [:span.tpc_content ])))
;                   (< 3 (.length titles)))
;
;            (try
;              (let [title (utils/filter-content titles)
;                    content (utils/filter-content (first (html/texts (html/select  detail [:span.tpc_content ]))))]
;                (println "title")
;                (println title)
;                (insert y-db/novel (values {:title title
;                                        :content content})))
;            (catch Exception e (str "caught exception: " (.getMessage e))))))))))
;
;(defn get-pin6-pic [start to]
;  ;获取图片数据
;  (doseq [i (range start to)]
;
;    (doseq [a (html/select
;              (try
;                (utils/get-page (str "http://www.p6we.com/thread.php?fid=39&search=&page=" i))
;              (catch Exception e (do (str "caught exception: " (.getMessage e)) ())))
;              [:td.t_one :a])]
;
;      (if-not (and (select y-db/novel (where {:title (html/text a)}))
;                   (re-find (re-matcher #"&page=" (first (html/texts (html/attr-values  a :href)))))
;                   (not (re-find (re-matcher #"read.php" (first (html/texts (html/attr-values  a :href)))))))
;
;        (let [titles (html/text a)
;
;              detail (try
;                        (utils/get-page (str "http://www.p6we.com/" (first (html/texts (html/attr-values  a :href)))))
;                      (catch Exception e (do (println (str "caught exception: " (.getMessage e))) ())))]
;
;
;          (if (and (first (html/texts (html/select  detail [:span.tpc_content ])))
;                   (< 3 (.length titles)))
;
;            (try
;              (let [title (utils/filter-content titles)
;                    content  (map #(first (html/attr-values % :src)) (html/select  detail [:span.tpc_content :img]))]
;                (doseq [url content]
;                  (utils/download (str "download/" url) url))
;                (exec-raw ["insert into pic(title, url_list) values(?, to_json(?))" [title, (json/write-str content)]] :results))
;                ;(insert y-db/pic (values {:title title
;                ;
;
;            (catch Exception e (str "caught exception: " (.getMessage e))))))))))
;
;
;
