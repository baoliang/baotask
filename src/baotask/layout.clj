(ns baotask.layout
  (:use fleet)
  (:require [clabango.parser :as parser]
            [fleet :refer [fleet fleet*]]
            [fleet.util :refer [within-ns]]
            [clojure.string :as s]
            [ring.util.response :refer [content-type response status]]
            [noir.session :as session]
            [baotask.views.helper]
            [baotask.config :as config])
  (:import compojure.response.Renderable))


(def template-path "views/templates/")

(parser/set-resource-base! template-path)

(def ^:private layout-caches (atom {}))

(defn clear-layout-caches []
(reset! layout-caches {}))

(defn create-fleet [template arg-names]
(let [template-str (parser/render-file template {})
      arg-names (map (comp symbol name) arg-names)
      tpl (within-ns 'baotask.views.helper 
                     (fleet* arg-names 
                             template-str 
                             {:escaping :xml 
                              :file-name template
                              :file-path template}))]
  tpl))

(defn get-fleet* [template arg-names]
(let [tpl (get @layout-caches template)]
  (if tpl
    tpl
    (let [tpl (create-fleet template arg-names)]
      (swap! layout-caches assoc template tpl)
      tpl))))

(def ^:private get-fleet (atom get-fleet*))

(defn set-layout-cache [on]
  (reset! get-fleet (if on get-fleet* create-fleet)))

(deftype RenderableTemplate [template data]
  Renderable
  (render 
    [this request]
    (let [tpl (@get-fleet template '[data template servlet-context request user static-url])
          res (str (tpl data
                        template
                        (:context request)
                        request
                        (try
                          (session/get :user)
                          (catch Exception e
                            {}))
                        (config/config "static_url")
                        ))]
      (-> res
          response
        (content-type "text/html; charset=utf-8")))))

(defn render 
  ([template] (render template {} ))
  ([template data]
     (RenderableTemplate. template data)))

(defn render-mail [template & [data]]
  (binding [baotask.views.helper/*email-subject* (atom nil)]
    (let [tpl (@get-fleet template '[data])
          ret {:body [{:type "text/html; charset=utf-8"
                       :content (str (tpl data))}]}
          subject @baotask.views.helper/*email-subject*
          ret (if subject (assoc ret
                            :subject subject)
                  ret)]
      ret)))

(defn render-msg [template & [data]]
(let [tpl (@get-fleet template '[data])
      ret (str (tpl data))]
  ret))

(defn resp [template data status-code]
  (let [template (if template template (str status-code ".html"))]
    (println template)
    (status (compojure.response/render (render template) data) status-code)))

(defn resp-400 [& [template data]]
(resp template data 400))

(defn resp-401 [& [template data]]
(resp template data 401))

(defn resp-403 [& [template data]]
(resp template data 403))

(defn resp-404 [& [template data]]
(resp template data 404))

(defn resp-500 [& [template data]]
(resp template data 500))
