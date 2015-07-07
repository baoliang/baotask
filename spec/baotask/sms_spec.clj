(ns baotask.task-spec
  (:require [baotask.utils :as utils]
            [speclj.core :refer :all]
            [baotask.bl.sms :as sms]))

(describe "Test 语言验证码"
	(it "Test 语音"
		(sms/send "13161984008" "222222" )))