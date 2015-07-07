(ns baotask.logo-spec
  (:require [baotask.log :as logo]
            [speclj.core :refer :all]))

(describe "utils test"
          (it "test get btc info"
              (should
                (nil? (logo/info "xx" "xx")))))

(run-specs)