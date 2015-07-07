(ns baotask.api
  (:import [net.sf.jlue.util Captcha])
  (:require [cheshire.core :refer :all]
            [postal.core :as postal]
            [baotask.utils :as utils]
            [baotask.bl.db :as db]
            [clojure.java.io :as io]
            [baotask.bl.sms :as sms]
            [baotask.storage :as st]
            [crypto.password.scrypt :as password]
            [baotask.mail :as mail]
            [baotask.layout :as layout])
  (import java.io.ByteArrayOutputStream
          javax.imageio.ImageIO
          java.awt.image.BufferedImage
          java.awt.image.BufferedImage))



(defn reg-user  "注册用户"
  [info]
  (if (db/query {:select-row [:*]
                            :from [:users]
                            :where [:= :phone (info "phone")]})

    {:success "uniqueness"}
    {:success (st/insert :users  {:info (utils/to-pg-json info)
                                  :username (info "username")
                                  :phone (info "phone")
                                  :password (password/encrypt (info "password"))})}))

(defn login-user
  "用户登陆"
  [info colum]
  (let [user (st/get-by-colum-value :user colum (info "email"))]
    (if user
      (if (password/check  (info "password")
                           (:password user))
        {:success (first user)}
        {:success "passwordwrong"})
      {:success "nouser"}))
  [info]
  (login-user info :email))

(defn check-forgot-password-phone [phone check-code]
  "校验邮件验证码"
  (let [check (st/kv-get "forgot-password-by-phone" (str phone check-code) check-code)]
    (if check
      (utils/from-pg-json (:value check))
        {})))


(defn update-password
  "更新密码"
  [phone password]
  (let [user (st/get-by-colum-value :users :phone phone)]
    (st/update-by-id :users (:id user) (password/encrypt password))))

(defn generate-forgot-password-phone "生成忘记密码验证码语音"
  [phone]
  (let [check-code (str (rand-int 6))]
    (st/insert :kvs
              {:scope "forgot-password-by-phone"
               :key (str phone check-code)
               :value (utils/to-pg-json {:value check-code :reset false})})
    (sms/send {:to phone}
              check-code)))



(defn generate-forgot-password-phone [phone]
  "生成验证码邮件"
  (let [check-code (str (rand-int 6))]
    (st/insert :kvs {:scope "forgot-password-by-email"
                     :key (str phone check-code)
                     :value (utils/to-pg-json {:value check-code :reset false})})
    (sms/send phone
               (layout/render-mail "mail/reset-password.html" {:code check-code :phone phone}))))

(defn gen-captcha
  "生成验证码"
  [width height text]
  (let [out (ByteArrayOutputStream.)]
    (ImageIO/write
      (let [captcha (doto (new Captcha))]
        (.gen captcha text width height))
      "png"
      out)
    (io/input-stream (.toByteArray out))))

;(defn check-valid-email-restpassword [email code]
;  (st db/kv (where {:key (str email code)})))

