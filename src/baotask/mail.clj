(ns baotask.mail
  (:refer-clojure :exclude [send])
  (:require [baotask.config :as config]
            [baotask.log :as log]
            [postal.core :refer [send-message]]))

(defn sendmail [mail & options]
  (let [msg (apply merge
                   mail
                   options)]
    (send-message msg)))

(defn send [mail  options]
  (log/info {:from (select-keys config/config ["mail" "from"])})
  (let [msg (merge {:from (select-keys config/config ["mail" "from"])}
                   mail
                   options)]
    (send-message  msg)))



