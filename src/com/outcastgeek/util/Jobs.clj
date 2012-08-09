(ns com.outcastgeek.util.Jobs
  (:use clojure.tools.logging
        [clojurewerkz.quartzite.jobs :only [defjob]]
        [clojurewerkz.quartzite.schedule.simple :only [schedule with-repeat-count with-interval-in-milliseconds]])
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j]))

(defjob NoOpJob
  [ctx]
  (debug "<<<< Does nothing >>>>"))

(defn runJobs []
  ;; starting Quartz Scheduler"""
  (qs/initialize)
;  (qs/start)
  (let [job (j/build
              (j/of-type NoOpJob)
              (j/with-identity (j/key "jobs.noop.1")))
        trigger (t/build
                  (t/with-identity (t/key "triggers.1"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (with-repeat-count 10)
                                     (with-interval-in-milliseconds 200))))]
    (qs/schedule job trigger)))
