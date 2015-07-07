(ns baotask.protocol.db)
(defprotocol P
  (foo [this])
  (bar-me [this] [this y]))
(deftype Foo [a b c]
  P
  (foo [this] a)
  (bar-me [this] b)
  (bar-me [this y] (+ c y)))