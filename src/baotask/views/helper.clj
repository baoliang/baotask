(ns baotask.views.helper
  (:require [clojure.string :as str]
            [clojure.data.json]
            [clojure.math.numeric-tower :as math]
            [baotask.utils :refer [url absolute-url] :as util]
            [fleet.runtime]
            [fleet.util]
            [noir.session :as session]
            [hiccup.core :refer [html]]
            [noir.request]
            [clj-time.core :as time]
            [clj-time.coerce :refer [to-date-time] :as cc]
            [clj-time.format]
            [clj-time.local]))

(defn fleet-raw [str]
     (fleet.runtime/raw fleet.util/escape-xml str))

(defn time-before? [first-time second-time]
  (time/before? first-time second-time))


(defn now []
  (time/now))

(defn get-user-session []
  "获取用户信息"
  (session/get! :user))

(defn json [data]
  (fleet-raw (clojure.data.json/write-str data)))

(defn datetime-format 
  "format refer: http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html"
  ([date] 
     (datetime-format date "yyyy-MM-dd HH:mm:ss"))
  ([date formatter] 
     (clj-time.format/unparse (if (or (string? formatter) (keyword? formatter)) 
                                (clj-time.format/formatter (name formatter) (time/time-zone-for-offset 8)) 
                                formatter) 
                              (to-date-time date))))
(defn date-format [date]
  (datetime-format date "yyyy-MM-dd"))

(defn to-sql-time [date-time]
  (cc/to-sql-time date-time))

(defn money-format 
  ([money] (money-format money 2))
  ([money f]
     (format (str "%,." f "f") (util/parse-float money))))

(defn money-wan [money]
  (let [v (util/float-round (/ money 10000.0) 1)]
    (if (== v 0)
      (money-format money)
      (str (format (if (== v (int v)) "%,.0f" "%,.1f") v) "万"))))

(defn percent-format 
  ([p]
     (percent-format p 2))
  ([p f]
     (str (format (str "%,." f "f") (float (* p 100))) "%")))

(defn js-void []
  "javascript:void(0);")

(defn script [name & [options]]
  (fleet-raw (html [:script (merge {:type "text/javascript"
                                    :src (str "/public/scripts/" name ".js")} 
                                   options)])))

(defn css [name & [options]]
  (fleet-raw (html [:link (merge {:media "all"
                                  :rel "stylesheet"
                                  :type "text/css"
                                  :href (str "/public/styles/" name ".css")} 
                                 options)])))

(defn image-path [path]
  (str "/files" path))
(defn image [path & [options]]
  (fleet-raw (html [:img (merge options {:src (str "/files" path)})])))


(defn local-now[]
  (clj-time.local/local-now))

(defn url-str [& params]
  (url (apply str params)))

(defn current-path []
  (:uri noir.request/*request*))

(defn url-startswith? [path]
  (.startsWith (str (:uri noir.request/*request*) "/") (if (.endsWith path "/") path (str path "/"))))

(defn menu-class-current! [path]
  (if (url-startswith? path)
    (fleet-raw "class=\"active\"")))


(def ^:dynamic *email-subject* (atom nil))

(defn set-email-subject! [subject]
  (reset! *email-subject* subject)
  nil)

(defmulti mask* (fn [value & [type]] type))

(defmethod mask* :email [value & _] 
  (let [[name domain] (str/split value #"@")
        name-length (count name)
        name (if (> name-length 3)
               (str (subs name 0 2)
                    (str/join (repeat (- name-length 3) "*"))
                    (subs name (- name-length 1)))
               (mask* name))]
    (str name
         "@" 
         domain)))

(defmethod mask* :mobile [value & _]
  (let [length (count value)]
    (if (= length 11)
      (str (subs value 0 3)
           (str/join (repeat 4 "*"))
           (subs value 7))
      (mask* value))))

(defmethod mask* :zh-name [value & _]
  (let [length (count value)]
    (if (> length 1)
      (if (> length 3) 
        (str (subs value 0 2)
             (str/join (repeat (- length 2) "*")))
        (str (subs value 0 1)
             (str/join (repeat (- length 1) "*"))))
      (mask* value))))

(defmethod mask* :id [value & _]
  (let [length (count value)]
    (if (> length 2)
      (str (subs value 0 2)
           (str/join (repeat (- length 2) "*")))
      (mask* value))))

(defmethod mask* :default [value & _]
  (clojure.string/join (repeat (count value) "*")))

(defn mask [value & [type]]
  (if value (mask* value type)))

(defn display-user [user]
  (some (fn [name] (if-not (clojure.string/blank? name) name)) (reverse (vals (select-keys user [:nickname :displayname :secure_email :secure_phone])))))


(defn pager [path & {:keys [params page count pagesize not-eq-page-size] :or {page 1 pagesize 10}}]
  (let [range-number pagesize
        half-span (math/ceil (/ range-number 2))
        page (if (> page 0) page 1)
        max-page (if (> count 0) (math/ceil (if not-eq-page-size (/ (- count 8) 12) (/ count pagesize))) 1)
        max-page (if (and not-eq-page-size (>= max-page 1)) (+ max-page 1) max-page)
        page (if (> page max-page) max-page page)
        [begin end] (if (<= page half-span) [1 range-number] [(- page (- half-span 1)) (+ page (- range-number half-span))])
        [begin end] (if (>= max-page end) [begin end] [(- max-page (- range-number 1)) max-page])
        begin (if (< begin 1) 1 begin)
        tmp (transient [:div {:class  (if (= 1 max-page) "none" "ui-paging")}])]
     
    
     (if (= page 1)
       (conj! tmp [:span {:class "ui-paging-info"} " <i class='iconfont' title='左三角形'>&#xF039;</i> 上一页"])
       (conj! tmp [:a {:class "ui-paging-prev " :href (str (url path (assoc params :page (- page 1))))}  " <i class='iconfont' title='左三角形'>&#xF039;</i> 上一页"]))
     (if (> page 5)
       (conj! tmp [:a {:class (if (= page 1) "ui-paging-item ui-paging-current" "ui-paging-item") :href (str (url path (assoc params :page 1)))}  1]))

    (doseq [i (range begin page)]
      (conj! tmp [:a {:class (if (= page i) "ui-paging-item ui-paging-current" "ui-paging-item") :href (str (url path (assoc params :page i)))} i]))

          (conj! tmp [:a { :class "ui-paging-item  ui-paging-current"  :href (str (url path (assoc params :page page)))}  page])
        
    (doseq [i (range (+ page 1) (+ end 1))]
      (conj! tmp [:a {:class (if (= page i) "ui-paging-item ui-paging-current" "ui-paging-item"):href (str (url path (assoc params :page i)))} i]))
    (if (> (- max-page page) 5)
      (conj! tmp [:span {:class "ui-paging-ellipsis"} "...."]))
    (if (> (- max-page page) 5)
      
      (conj! tmp [:a {:class (if (= page max-page) "ui-paging-item ui-paging-current" "ui-paging-item")   :href (str (url path (assoc params :page max-page)))}  max-page]))
    (conj! tmp [:form { :action (str (url path params)) :class (if (> (- max-page page) 5) "inline" "none")} [:span {:class "ui-paging-which "} [:input {:name "page" :value page}]] [:input  {:type "submit" :class "ui-paging-info ui-paging-goto" :href (str (url path params))}]])

    (conj! tmp (if (not= page max-page)
                 [:a {:class "ui-paging-prev" :href (str (url path (assoc params :page (+ page 1))))}  "下一页 <i class='iconfont' title='右三角形'>&#xF03A;</i>"]
                 [:span {:class "ui-paging-info"}  "下一页 <i class='iconfont' title='右三角形'>&#xF03A;</i>"]))

    (fleet-raw (html [:div {:class "pub-page"} (persistent! tmp) [:div {:class "clear"}]]))))

(defn remove-end [s ]
  "去掉最后一个字符"
  (subs s 0 (- (.length s) 1)))




(defn split-string--max-len-to-dot [string len]
  (if (> (count string) len)
    (str (subs string 0 len) "...")
    string))

