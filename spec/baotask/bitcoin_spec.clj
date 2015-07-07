(ns baotask.bitcoin-spec
  (:require [baotask.bitcoin :as bitcoin]
            [speclj.core :refer :all]))

(describe "utils test"
  (it "test get energycoin info"
      (should
        (not-empty (bitcoin/mobilecoin :getinfo))
        )))

(run-specs)