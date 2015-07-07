(ns baotask.redis-session
  (:require [ring.middleware.session.store :as session-store]
            [clj-time.core :as time]
            [taoensso.carmine :as car :refer (wcar)]))

(defrecord RuiSessionStore [conn-opts prefix ttl-secs]
  session-store/SessionStore
  (read-session   [_ key] (let [r (or (when key (wcar conn-opts (car/get key))) {})
                                {update-time ::update-time} (meta r)]
                            (if update-time
                              (let [sec (time/in-seconds (time/interval update-time (time/now)))
                                    half-ttl (/ ttl-secs 2)]
                                (if (< half-ttl sec)
                                  (wcar conn-opts (car/setex key (int (* ttl-secs 1.5)) (with-meta r {::update-time (time/now)}))))
                                r)
                              {})))
  (delete-session [_ key] (wcar conn-opts (car/del key)) nil)
  (write-session  [_ key data]
    (let [data (with-meta data {::update-time (time/now)})
          key (or key (str prefix ":" (java.util.UUID/randomUUID)))]
      (wcar conn-opts (if ttl-secs (car/setex key (int (* ttl-secs 1.5)) data)
                                   (car/set   key          data)))
      key)))



(defn redis-store
  "Creates and returns a Carmine-backed Ring SessionStore. Use `expiration-secs`
  to specify how long session data will survive after last write. When nil,
  sessions will never expire."
  [conn-opts & [{:keys [key-prefix expiration-secs]
                 :or   {key-prefix      "carmine:session"
                        expiration-secs (* 60 60 2)}}]]
  (->RuiSessionStore conn-opts key-prefix expiration-secs))
