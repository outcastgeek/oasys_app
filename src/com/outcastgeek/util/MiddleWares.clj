(ns com.outcastgeek.util.MiddleWares
  (:use compojure.handler
        ring.middleware.reload
        ring.middleware.etag
        ring.middleware.json
        ring.middleware.gzip
        somnium.congomongo
        [clojure.string :only [upper-case]]
        com.outcastgeek.util.Sessions
        com.outcastgeek.config.AppConfig)
  (:require [cheshire.core :refer [parse-string]]
            [clojure.tools.logging :as log]
            [ring.middleware.session :as rs]
            [hozumi.mongodb-session :as mongoss]))

(set-connection! mongo-connection)

(def mongoSessionStore
  (mongoss/mongodb-store {:auto-key-change? true
                          :collection-name sessionsCollection}))

;;  Request logger
(defn wrap-request-logger [handler]
  (fn [req]
    (let [{remote-addr :remote-addr request-method :request-method uri :uri} req]
      (log/debug remote-addr (upper-case (name request-method)) uri)
      (handler req))))

;; Response logger
(defn wrap-response-logger [handler]
  (fn [req]
    (let [response (handler req)
          {remote-addr :remote-addr request-method :request-method uri :uri} req
          {status :status} response]
      (log/debug remote-addr (upper-case (name request-method)) uri "->" status)
      response)))

;; Adds a bunch of useful ring middlewares
(defn wrap-og-filters
  [routes]
  (-> routes
;    wrap-reload ; TODO: Investigate why this does not work!!!!
;    wrap-response-logger
    wrap-request-logger
    wrap-json-params
    site
;    wrap-etag ; TODO: Investigate why this does not work!!!!
    wrap-gzip
    (wrap-session-expiry sessionDuration)
    (rs/wrap-session {:cookie-name sessionName
                      :store mongoSessionStore})))

