(ns com.outcastgeek.util.Jobs
  (:use somnium.congomongo
        com.outcastgeek.config.AppConfig
        [clojurewerkz.quartzite.jobs :only [defjob]]
        [clojurewerkz.quartzite.schedule.simple :only [schedule repeat-forever with-interval-in-minutes]])
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j])
  (:import java.util.Date))

(defjob SessionsCleaner
  [ctx]
  (cleanExpiredSessions))

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
