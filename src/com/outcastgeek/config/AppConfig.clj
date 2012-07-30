(ns com.outcastgeek.config.AppConfig
  (:use clojure.java.io
        somnium.congomongo)
  (:require [resque-clojure.core :as resque])
  (:import java.util.Date
           java.sql.Timestamp
;           org.springframework.context.support.ClassPathXmlApplicationContext
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

;;;;;;;;;;;;;;;;; STORAGE ;;;;;;;;;;;;;;;;;;;;;;;;

(def dbName (appProperties :mongo-database))

(def mongo-connection
  (make-connection
   dbName
   ;{:host "localhost" :port 27019}
   ;{:host "localhost" :port 27018}
   {:host (appProperties :mongo-url) :port (appProperties :mongo-port)}
   (MongoOptions.)))
;(debug "Connected to Replica Set.")

(def connection
  (Mongo.
   (list
    ;(ServerAddress. "127.0.0.1" 27019)
    ;(ServerAddress. "127.0.0.1" 27018)
    (ServerAddress. (appProperties :mongo-url) (appProperties :mongo-port)))
   (MongoOptions.)))

;;;;;;;;;;;;;;;; MESSAGING ;;;;;;;;;;;;;;;;;;;

(resque/configure {:host (appProperties :redis-url) :port (appProperties :redis-port)}) ;; optional

;(def appCtx
;  (ClassPathXmlApplicationContext. "spring/applicationContext.xml"))

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
