(ns com.outcastgeek.config.AppConfig
  (:use clojure.tools.logging
        clojure.java.io
        somnium.congomongo
        clamq.protocol.connection
        clamq.jms
        [clojure.java.io :only [reader]])
  (:require [resque-clojure.core :as resque]
            [clamq.protocol.connection :as qcon]
            [clamq.protocol.consumer :as cons]
            [clamq.protocol.producer :as prod]
            [clj-time.core :as time])
  (:import java.util.Date
           java.sql.Timestamp
           com.mongodb.Mongo
           com.mongodb.ServerAddress
           com.mongodb.MongoOptions
           com.outcastgeek.util.QueueServer))

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

;;;;;;;;;;;;;;;;;;;    QUEUES    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(resque/configure {:host (appProperties :redis-url)
                   :port (appProperties :redis-port)
                   :max-workers (appProperties :redis-workers)}) ;; optional

(def employeeQueue (appProperties :employee-queue))

(def mailQueue (appProperties :mail-queue))

(def queueServer
  (QueueServer/createQueueServer (appProperties :queue-server-conf-url)
                                 (appProperties :queue-server-host)
                                 (appProperties :queue-server-port)))

(def jmsServerManager
  (QueueServer/createJMSServerManager queueServer
                                      (appProperties :queue-manager-conf-url)))

(def queueServerConnectionFactory
  (QueueServer/createConnectionFactory (appProperties :queue-server-conf-url)
                                       (appProperties :queue-server-host)
                                       (appProperties :queue-server-port)))

(def jmsConnection
  (jms-connection queueServerConnectionFactory
                  (fn [] (. queueServerConnectionFactory close))))

(defn qConsumer [queue listener]
  (let [consu (qcon/consumer jmsConnection
                             {:endpoint queue
                              :on-message listener
                              :transacted false})]
    consu))

(defn qProducer [options]
  (let [produ (qcon/producer jmsConnection
                             options)]
    produ))

(def queueProducer
  (qProducer {:pubSub false}))

(defn qPublish [queue message]
  (prod/publish queueProducer
                queue
                message))

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