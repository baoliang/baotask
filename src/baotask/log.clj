(ns baotask.log
  (:require [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [hiccup.core :refer [html h]]
            [hiccup.page :refer [html5]]
            [clj-stacktrace.core :as cc]
            [clj-stacktrace.repl :as cr]
            [postal.core :refer [send-message]]
            [baotask.config :as config]))

(defn- style-resource [path]
  (html [:style {:type "text/css"} (slurp (io/resource path))]))

(defn- elem-partial [elem]
  (if (:clojure elem)
    [:tr.clojure
     [:td.source (h (cr/source-str elem))]
     [:td.method (h (cr/clojure-method-str elem))]]
    [:tr.java
     [:td.source (h (cr/source-str elem))]
     [:td.method (h (cr/java-method-str elem))]]))

(defn- html-exception [ex context]
  (let [[ex & causes] (iterate :cause (cc/parse-exception ex))]
    (html5
     [:head
      [:title "Ring: Stacktrace"]
      (style-resource "ring/css/stacktrace.css")]
     [:body
      [:div#exception
       [:h1 (h (.getName ^Class (:class ex)))]
       [:div.message (h (:message ex))]
       [:div.trace
        [:table
         [:tbody (map elem-partial (:trace-elems ex))]]]
       (for [cause causes :while cause]
         [:div#causes
          [:h2 "Caused by " [:span.class (h (.getName ^Class (:class cause)))]]
          [:div.message (h (:message cause))]
          [:div.trace
           [:table
            [:tbody (map elem-partial (:trace-elems cause))]]]])]
      [:div#context (prn-str context)]])))

(defn mail-exception [title ex context]
  (if-let [to (:mail "24750197@qq.com")]
    (let [name (try 
                 (.getHostName (java.net.InetAddress/getLocalHost))
                 (catch Exception ex
                   "rui"))
          from (str name "@baoliang.com")]
      (send-message {:from from
                     :to to
                     :subject title
                     :body [{:type "text/html"
                             :content (html-exception ex context)}]}))))



(defn info [info & options]
  (timbre/info info options))

(defn waring [info]
  )

(defn fatal [exception & [context]]
  (timbre/fatal exception)
  (mail-exception "fatal" exception context))
