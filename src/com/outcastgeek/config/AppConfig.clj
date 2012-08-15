(ns com.outcastgeek.config.AppConfig
  (:use clojure.tools.logging
        clojure.java.io
        somnium.congomongo
        [clojure.java.io :only [reader]])
  (:require [resque-clojure.core :as resque]
            [clojure.core.reducers :as r])
  (:import java.util.Date
           java.sql.Timestamp
           com.mongodb.Mongo
           com.mongodb.ServerAddress
           com.mongodb.MongoOptions))

(defn get-current-timestamp []
  (Timestamp. (. (Date.) getTime)))

(defn load-props
  [file-name]
  (with-open [^java.io.Reader reader (clojure.java.io/reader file-name)] 
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [(keyword k) (read-string v)])))))

(def appProperties
  (load-props "app.properties"))

;(def props
;  (doto (Properties.)
;    (.load (reader "app.properties"))))

;;;;;;;;;;;;;;;;; STORAGE ;;;;;;;;;;;;;;;;;;;;;;;;

(def dbName (str (appProperties :mongo-database)))

(def sessionsCollection (keyword (appProperties :sessions-collection)))

;The MongoURI spec:
;http://www.mongodb.org/display/DOCS/Connections
(def mongo-connection
  (make-connection dbName
                   {:host (appProperties :mongo-host)
                    :port (appProperties :mongo-port)}
                   (mongo-options :auto-connect-retry true)))
;(debug "Connected to Replica Set.")
(set-write-concern mongo-connection :safe) ;Consult documentation

;;;;;;;;;;;;;;;; MESSAGING ;;;;;;;;;;;;;;;;;;;

(resque/configure {:host (appProperties :redis-url) :port (appProperties :redis-port)}) ;; optional

(def employeeQueue (appProperties :employee-queue))

(def mailQueue (appProperties :mail-queue))

;;;;;;;;;;;;;;;;; User Sessions ;;;;;;;;;;;;;;;;;;

(def sessionName (appProperties :session-name))

(def sessionDuration (appProperties :session-duration))

(def sessionsCleaner (appProperties :session-cleaner-name))

(def sessionsTrigger (appProperties :session-cleaner-trigger))

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
(defn expired? [date]
  (< sessionDuration
     (- (.getTime (Date.)) (.getTime date))))

(defn destroyExpiredSessions [sess]
  (let [sessionId (sess :_id)]
    (debug "<<<< Destroying expired user session with Id: " sessionId " >>>>")
    (destroy! sessionsCollection
              {:_id sessionId})))

(defn grapSomeSessions []
  (debug "<<<< Grabbing some user sessions.... >>>>")
  (fetch sessionsCollection
         :limit 44))

(defn filterOutExpiredSessions [sessions]
  (debug "<<<< Filtering out expired user sessions.... >>>>")
  (into () (filter #(expired? (% :session_timestamp)) sessions)))

(defn cleanExpiredSessions []
  (debug "\n\n<<<< Cleaning expired user sessions.... >>>>")
  (r/map #((if (expired? %)
                 (destroyExpiredSessions %))) (grapSomeSessions))
  (debug "<<<< Done cleaning expired user sessions.... >>>>\n\n"))
