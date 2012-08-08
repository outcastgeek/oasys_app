(ns com.outcastgeek.config.AppConfig
  (:use clojure.java.io
        somnium.congomongo
        [clojure.java.io :only [reader]])
  (:require [resque-clojure.core :as resque])
  (:import java.util.Date
           java.sql.Timestamp
;           java.util.Properties
;           javax.persistence.Persistence
;           com.outcastgeek.config.JavaConfig
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

(def dbName (appProperties :mongo-database))

;The MongoURI spec:
;http://www.mongodb.org/display/DOCS/Connections
(def mongo-connection
  ;note that authentication is handled when given a user:pass@ section
  ;(make-connection "mongodb://user:pass@host:27071/databasename")
  (make-connection (str "mongodb://localhost:27017/" dbName)))
;(debug "Connected to Replica Set.")

;;;;;;;;;;;;;;;; MESSAGING ;;;;;;;;;;;;;;;;;;;

(resque/configure {:host (appProperties :redis-url) :port (appProperties :redis-port)}) ;; optional

;(def appCtx
;  (JavaConfig/getContext))
;
;(def entityManager
;  (. appCtx getBean "entityManagerFactory"))

;(def entityManager
;  (. (Persistence/createEntityManagerFactory "persistenceUnit" props) createEntityManager))
;
;(defmacro with-transaction
;  [& body]
;  `(let [tx# (.getTransaction entityManager)]
;     (.begin tx#)
;     ~@body
;     (.commit tx#)))

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
