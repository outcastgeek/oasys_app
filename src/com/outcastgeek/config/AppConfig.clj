(ns com.outcastgeek.config.AppConfig
  (:use clojure.tools.logging
        clojure.java.io
        somnium.congomongo
        [clojure.java.io :only [reader]])
  (:require [resque-clojure.core :as resque])
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

;;;;;;;;;;;;;;;;; APPLICATION ;;;;;;;;;;;;;;;;;;;;;;;;

(def appName (appProperties :app-name))

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

;;;;;;;;;;;;;;;;; E-MAIL ;;;;;;;;;;;;;;;;;;

(def smtpHost (appProperties :amz-smtp-host))

(def smtpPort (appProperties :amz-smtp-port))

(def smtpUser (appProperties :amz-smtp-user))

(def smtpPwd (appProperties :amz-smtp-pwd))

(def smtpSender (appProperties :amz-smtp-sender))


