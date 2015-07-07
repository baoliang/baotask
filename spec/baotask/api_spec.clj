(ns baotask.api-spec
  (:require [baotask.api :as api]
  			    [speclj.core :refer :all]
            [baotask.db :as db]
            [baotask.utils :as utils]))

;(def user {"password" "123"
;           "username" ""
;           "email" "test@test.com"})
;
;(describe "api test"
;  ;
;  ;(before
;  ;  (korma/delete db/users (korma/where {:email "test@test.com"})))
;
;  ;(after
;  ;  (korma/delete db/users (korma/where {:email "test@test.com"})))
;
;  (it "test crate and login user"
;     (should
;      (not= "uniqueness" (:success (api/reg-user  user))))
;
;     (should
;       (= "uniqueness" (:success (api/reg-user  user))))
;
;     (should
;       (= false (api/check-forgot-password-email "jfreebird@gmail.com" "xxxx")))
;
;     (should
;       (= true (:success (api/login-user user))))
;
;     (should
;       (= "nouser" (:success (api/login-user (assoc user "email" "xx" )))))
;
;     (should
;       (= "passwordwrong" (:success (api/login-user (assoc user "password" "1"))))))
;
;  (it "test send email"
;    (should (= "xxx" (:text (api/gen-captcha 111 50 "xxx"))))
;    (should (= :SUCCESS (:error (api/send-email
;                     "jfreebird@conext.com"
;                     "24750197@qq.com"
;                     ""
;                     "test send_emal"
;
;                     "<div><img src='http://img0.bdstatic.com/img/image/2379c16fdfaaf51f3de957ee27396eef01f3b2979dc.jpg'></div>div>"))))))
;
;
;(run-specs)