(ns baotask.bitcoin
  (:use [pawnshop.core])
  (:require [baotask.utils :as util]
            [baotask.config :refer [config]]))



(def mobilecoin (bitcoin-proxy (get-in config ["mobilecoin" "rpchost"])
                  (get-in config ["mobilecoin" "rpcuser"])
                  (get-in config ["mobilecoin" "rpcpassword"])))


(def energycoin (bitcoin-proxy (get-in config ["energycoin" "rpchost"])
                               (get-in config ["energycoin" "rpcuser"])
                               (get-in config ["energycoin" "rpcpassword"])))


