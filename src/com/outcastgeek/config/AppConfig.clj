(ns com.outcastgeek.config.AppConfig
  (:use clojure.tools.logging
        clojure.java.io
        somnium.congomongo
        [clojure.java.io :only [reader]])
  (:require [clj-time.core :as time]
            ;[resque-clojure.core :as resque]
            )
  (:import java.util.Date
           java.sql.Timestamp
           com.mongodb.Mongo
           com.mongodb.ServerAddress
           com.mongodb.MongoOptions))

(defn get-current-timestamp []
  (Timestamp. (. (Date.) getTime)))

(defn firstDayOfTheMonthOf [date]
  (let [cyear (time/year date)
        cmonth (time/month date)]
    (time/date-time cyear cmonth 1)))

(defn lastDayOfTheMonthOf [date]
  (let [oneMonthFromDate (time/plus date (time/months 1))
        cyear (time/year oneMonthFromDate)
        cmonth (time/month oneMonthFromDate)
        firstOfNextMonth (time/date-time cyear cmonth 1)]
    (time/minus firstOfNextMonth (time/days 1))))

(defn firstDayOfTheWeekOf [date]
  (let [cday (time/day-of-week date)]
    (time/minus date (time/days (+ 1 cday)))))

(defn lastDayOfTheWeekOf [date]
  (let [cday (time/day-of-week date)]
    (time/plus date (time/days (- 7 cday)))))

(defn load-props
  [file-name]
  (with-open [^java.io.Reader reader (clojure.java.io/reader file-name)] 
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [(keyword k) (read-string v)])))))

(def appProperties
  (load-props "app.properties"))

(def appName (appProperties :app-name))

(def dbName (str (appProperties :mongo-database)))

(def sessionsCollection (keyword (appProperties :sessions-collection)))

(def mongo-connection
  (make-connection dbName
                   {:host (appProperties :mongo-host)
                    :port (appProperties :mongo-port)}
                   (mongo-options :auto-connect-retry true)))

(set-write-concern mongo-connection :safe) ;Consult documentation

;;;;;;;;;;;;;;;;;;;    QUEUES

;(resque/configure {:host (appProperties :redis-url)
;                   :port (appProperties :redis-port)
;                   :namespace (appProperties :redis-namespace)}) ;; optional

(defn enQueueStuff [name]
  (debug name)
  ;(resque/enqueue "my_job_queue" "MyJob" name)
  (debug "Done enqueueing MyJob!!!!"))

(def employeeQueue (appProperties :employee-queue))

(def mailQueue (appProperties :mail-queue))

;;;;;;;;;;;;;;;;;;;    END QUEUES    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def sessionName (appProperties :session-name))

(def sessionDuration (appProperties :session-duration))

(def sessionsCleaner (appProperties :session-cleaner-name))

(def sessionsTrigger (appProperties :session-cleaner-trigger))

(def payrollCreator (appProperties :payroll-creator-name))

(def payrollTrigger (appProperties :payroll-creator-trigger))

(def smtpHost (appProperties :amz-smtp-host))

(def smtpPort (appProperties :amz-smtp-port))

(def smtpUser (appProperties :amz-smtp-user))

(def smtpPwd (appProperties :amz-smtp-pwd))

(def smtpSender (appProperties :amz-smtp-sender))
