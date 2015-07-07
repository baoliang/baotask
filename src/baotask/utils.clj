(ns baotask.utils
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [compojure.core]
            [clojure.string]
            [clojure.tools.macro :refer [name-with-attributes]]
            [noir.request]
            [crypto.password.scrypt :as password]
            [cheshire.core]
            [cheshire.generate]
            [uri.core :as uri]
            [clj-stacktrace.core :refer [parse-exception]]
            [postal.core :refer [send-message]]
            [clj-time.core :as time]
            [clj-time.local :as local]
            [clj-time.coerce :refer [to-sql-time to-date-time from-sql-time to-long]]
            [clj-time.format :as tf])
    (:import [java.io File]
           [java.net InetAddress]
           [org.postgresql.util PGobject]))


(defn get-config
  ;获取配置
  ([] (json/read-str (slurp "./config.json")))
  ([file] (json/read-str (slurp file))))

(def config (get-config))

(defn to-pg-json [obj]
  ;转为PostgreSQL json
  (doto (PGobject.)
        (.setType "jsonb")
        (.setValue (json/write-str obj))))



(defn get-btc-fun [func]
  (let [config ((get-config) "mobilecoin")]
    (func :config {:rpcpassword (config "rpcpassword")
                         :rpcuser (config "rpcuser")
                         :rpcport (config "rpcport")
                         :rpchost (config "rpchost")})))

(defn time-stamp-now
  []
  (to-long (local/local-now)))

(defn from-pg-json [pg]
  ;从pgobject 对像转为clojure对像
  (json/read-str (.getValue pg)))

; (defn get-page [url]
; 	;获取页面html
;   (html/html-snippet (hiccup/html (tagsoup/parse url))))

(defn filter-content [obj]
	;过滤爬虫数据
	(if (= "clojure.lang.PersistentVector" (class obj))
			(first obj)
			obj))

(defn uuid [] 
	;生成uuid
	(str (java.util.UUID/randomUUID)))

(defn download [to from]
 ;存储文件到指定目录
  (with-open [in  (io/input-stream (io/as-url from))]
    (io/copy in (File. to))))




;; server info
(defn local-ipaddress []
  (.getHostAddress (InetAddress/getLocalHost)))

;; server info end
(defn midnight 
  ([] (midnight (time/now)))
  ([date]
     (let [mid (time/date-time (time/year date) (time/month date) (time/day date) 16)]
       (if (time/after? mid date)
         (time/minus mid (time/days 1))
         mid))))

;; database 

(defn expire-time [span]
  (time/plus (time/now) span))

(defn expire-sql-time [span]
  (to-sql-time (expire-time span)))

(defn get-db-duplicated-key [ex]
  (let [state (.getSQLState ex)
        msg (.getMessage ex)]
    (if (and 
         (= "23000" state))
      (let [re (re-find #"^Duplicate entry '.*' for key '(\w+)'$" msg)]
        (if re
          (keyword (nth re 1)))))))


(defn datetime-format
  "format refer: http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html"
  ([date]
   (datetime-format date "yyyy-MM-dd HH:mm:ss"))
  ([date formatter]
   (clj-time.format/unparse (if (or (string? formatter) (keyword? formatter))
                              (clj-time.format/formatter (name formatter) (time/time-zone-for-offset 8))
                              formatter)
                            (to-date-time date))))

;;database end

;; http response
(cheshire.generate/add-encoder java.sql.Date (fn [c jsonGenerator]
                                                (.writeString jsonGenerator (datetime-format c "yyyy-MM-dd"))))
(cheshire.generate/add-encoder java.sql.Timestamp (fn [c jsonGenerator]
                                                    (.writeString jsonGenerator (datetime-format c))))
(cheshire.generate/add-encoder java.util.Date (fn [c jsonGenerator]
                                                (.writeString jsonGenerator (datetime-format c))))
(cheshire.generate/add-encoder org.joda.time.DateTime (fn [c jsonGenerator]
                                                        (.writeString jsonGenerator (datetime-format c))))



(defmacro if-html [html & [json]]
  `(let [r# noir.request/*request*]
     (if-not (get (:headers noir.request/*request*) "x-requested-with")
       ~html ~json)))
;; http response end

(defn parse-int 
  ([value]
     (parse-int value 0))
  ([value default]
     (let [value (str value)]
       (if (re-find #"^-?\d+$" value)
         (try 
           (Integer/parseInt value)
           (catch Exception e
             default))
         default))))

(defn parse-float 
  ([value]
     (parse-float value 0.0))
  ([value default]
     (let [value (str value)]
       (if (re-find #"^-?\d+(\.\d+)?$" value)
         (try
          (Float/parseFloat value)
          (catch Exception e
            default))
         default))))

(defn- float-round- [value scale round-type]
  (let [d (java.math.BigDecimal. (double value))
        d (.setScale d scale round-type)]
    (.floatValue d)))

(defn float-roundup [value scale]
  (float-round- value scale java.math.BigDecimal/ROUND_UP))

(defn float-rounddown [value scale]
  (float-round- value scale java.math.BigDecimal/ROUND_DOWN))

(defn float-roundhalf [value scale]
  (float-round- value scale java.math.BigDecimal/ROUND_HALF_UP))

(def float-round float-roundhalf)

(def ^:private date-formatter (tf/formatter "yyyy-MM-dd"))

(defn parse-date [value]
  (try
    (tf/parse date-formatter (str value))
    (catch Exception ex
      nil)))

(defn email-or-phone? [value]
  (let [value (str value)]
    (if (re-matches #"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}" value)
      :email
      (if (re-matches #"[0-9]{11}" value)
        :phone))))

(defn url [path & [params]]
  (uri/make {:path path
             :query (if (> (count params) 0) params)}))

(defn str-url [path & [params]]
  (str (url path params)))

(defn absolute-url [path & [params]]
  (str (config "site")
       (url path params)))

(defn to-sql-like [s]
  "把字符串转为sql like字符串"
  (if s
    (str "%" s "%")
    nil))

(defmacro fmt [^String string]
  (let [-re #"#\{(.*?)\}"
        fstr (clojure.string/replace string -re "%s")
        fargs (map #(read-string (second %)) (re-seq -re string))]
    `(format ~fstr ~@fargs)))



(defn list-contains? [coll value]
  "判断列表是否有一项值"
  (let [s (seq coll)]
    (if s
      (if (= (first s) value) true (recur (rest s) value))
      false)))

(defn en-password [password]
  (password/encrypt password))

(defn password-check [password s-password]
  (password/check  password s-password))

(defn float-roundup [value scale]
  (float-round- value scale java.math.BigDecimal/ROUND_UP))

(defn float-rounddown [value scale]
  (float-round- value scale java.math.BigDecimal/ROUND_DOWN))

(defn float-roundhalf [value scale]
  (float-round- value scale java.math.BigDecimal/ROUND_HALF_UP))

(defn map-key-to-keyword [map-value]
  (into {}
        (for [[k v] map-value]
          [(keyword k) v])))

(defn get-current-day-sql-time-start []
  (to-sql-time (datetime-format (local/local-now) "yyyy-MM-dd")))
