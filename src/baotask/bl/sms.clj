(ns baotask.bl.sms
    (:refer-clojure :exclude [send])

    (:require [clj-http.client :as client]
              [clojure.xml :as xml]
              [digest]
              [base64-clj.core :as base64]
              [baotask.utils :as utils]
              [baotask.storage :as st]
              [clj-time.local :as loacal]
              [taoensso.timbre :as timbre]
              [clojure.data.json :as json]))

(def config {:account-sid "aaf98f89488747b20148889ddbce0119"
             :auth-token "b7d46625995142ffb4285c4abee47788"})

(defn send [phonenumbers txt & [public]]
      "发送短信 phonenumbers 格式为字符串向量 e.g [\"13161984009\" \"13161984008\"]
      ;返回值  0 = 发送失败 -2 = 网络一场 0 -2 之外为失败"

      (let [phonenumbers (clojure.string/join ","  (if (string? phonenumbers)
                                                     [phonenumbers]
                                                     phonenumbers))
            time-stamp (utils/datetime-format (loacal/local-now) "yyyyMMddHHmmss")
            res (try
                    (client/post
                      (format "https://sandboxapp.cloopen.com:8883/2013-12-26/Accounts/%s/Calls/VoiceVerify?sig=%s"
                              (:account-sid config)
                              (digest/md5 (str (:account-sid config)
                                               (:auth-token config)
                                               time-stamp)))
                      {:headers {
                                  :Authorization (base64/encode
                                                   (str (:account-sid config)
                                                        ":"
                                                        time-stamp))}
                        :content-type :json
                        :accept :json

                        :body (json/write-str
                                {:appId "aaf98f89488747b2014888ace33d011c"
                                 :verifyCode txt
                                 :displayNum "13161984009"
                                 :to  phonenumbers})
                        :body-encoding "UTF-8"})
                  (catch Exception e
                    (timbre/fatal e)
                    {:body "{\"msg\" \"服务异常\"}"}))
            - (println res)
            body (str (:body res))]
           (st/kv-insert "voice_vertify"
                         (str "voice_vertify_" phonenumbers "_" txt)
                         {:numbers phonenumbers
                          :content txt
                          :result body})
           body))




