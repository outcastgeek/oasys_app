(ns com.outcastgeek.util.Sessions
  (:use clojure.tools.logging
        somnium.congomongo
        com.outcastgeek.config.AppConfig)
  (:require [clojure.core.reducers :as r])
  (:import java.util.Date))

(set-connection! mongo-connection)

;Borrowed from here: https://raw.github.com/hozumi/session-expiry/master/src/hozumi/session_expiry.clj

(defn- expire? [date expire-ms]
  (< expire-ms
     (- (.getTime (Date.)) (.getTime date))))

(defn wrap-session-expiry [handler expire-sec]
  (let [expire-ms (* 1000 expire-sec)]
    (fn [{{timestamp :session_timestamp :as req-session} :session :as request}]
      (let [expired?  (and timestamp (expire? timestamp expire-ms))
            request  (if expired?
                       (assoc request :session {})
                       request)
            response (handler request)]
        (if (contains? response :session)
          (if (response :session)
            ;;write-session and update date
            (assoc-in response [:session :session_timestamp] (Date.)) 
            ;;delete-session because response include {:session nil}
            response)
          (if (empty? req-session)
            response
            (if expired?
              ;;delete-session because session is expired
              (assoc response :session nil)
              ;;update date
              (assoc response :session (assoc req-session :session_timestamp (Date.)))))
          ))
      )))
(defn- expired? [date]
  (< sessionDuration
     (- (.getTime (Date.)) (.getTime date))))

(defn- destroyExpiredSessions [sess]
  (let [sessionId (sess :_id)]
    (debug "<<<< Destroying expired user session with Id: " sessionId " >>>>")
    (destroy! sessionsCollection
              {:_id sessionId})))

(defn- grapSomeSessions []
  (debug "<<<< Grabbing some user sessions.... >>>>")
  (fetch sessionsCollection
         :limit 24000))

(defn- filterOutExpiredSessions [sessions]
  (debug "<<<< Filtering out expired user sessions.... >>>>")
  (into () (filter #(expired? (% :session_timestamp)) sessions))
;  (into () (r/filter #(expired? (% :session_timestamp)) sessions))
  )

(defn cleanExpiredSessions []
  (debug "\n\n<<<< Cleaning expired user sessions.... >>>>")
  (doall
    (pmap destroyExpiredSessions (filterOutExpiredSessions (grapSomeSessions))))
;  (do
;    (r/map #((if (expired? %)
;                 (destroyExpiredSessions %))) (grapSomeSessions)))
  (debug "<<<< Done cleaning expired user sessions.... >>>>\n\n"))


