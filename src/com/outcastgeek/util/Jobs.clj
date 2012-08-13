(ns com.outcastgeek.util.Jobs
  (:use clojure.tools.logging
        somnium.congomongo
        com.outcastgeek.config.AppConfig
        [clojurewerkz.quartzite.jobs :only [defjob]]
        [clojurewerkz.quartzite.schedule.simple :only [schedule repeat-forever with-interval-in-minutes]])
  (:require [clojure.core.reducers :as r]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j])
  (:import java.util.Date))

(set-connection! mongo-connection)

(defjob SessionsCleaner
  [ctx]
  (debug "\n\n<<<< Cleaning expired user sessions.... >>>>")
  (let [expired? (fn [date]
                   (< sessionDuration
                      (- (.getTime (Date.)) (.getTime date))))
        sessions (fetch sessionsCollection
                        :limit 44)
        destroyExpiredSessions (fn [sess]
                                 (let [sessionId (sess :_id)]
                                   (debug "<<<< Destroying expired user session with Id: " sessionId " >>>>")
                                   (destroy! sessionsCollection
                                             {:_id sessionId}))
                                 )]
    (doall
      (pmap destroyExpiredSessions (into [] (r/filter #(expired? (% :session_timestamp))
                                           (r/map
                                            #(select-keys % [:session_timestamp :_id]) sessions)))))
    (debug "<<<< Done cleaning expired user sessions.... >>>>\n\n")))

(defn runJobs []
  ;; starting Quartz Scheduler"""
  (qs/initialize)
  (qs/start)
  (let [sessionsCleanerJob (j/build
              (j/of-type SessionsCleaner)
              (j/with-identity (j/key sessionsCleaner)))
        sessionsCleanerTrigger (t/build
                  (t/with-identity (t/key sessionsTrigger))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (repeat-forever)
                                     (with-interval-in-minutes 1))))]
    (qs/schedule sessionsCleanerJob sessionsCleanerTrigger)))
