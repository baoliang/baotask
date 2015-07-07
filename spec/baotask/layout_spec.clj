(ns baotask.api-spec
  (:require [baotask.layout :as layout]
            [speclj.core :refer :all]))

(describe "test layout template"
  (it "test layout"
      (should
        (not-empty (layout/render "test.html" {:title "test"}))))
  (it "test resp"
      (should
        (not-empty (layout/resp nil nil 500)))))

(run-specs)