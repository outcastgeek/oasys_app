(ns com.outcastgeek.util.Jobs
  (:use com.outcastgeek.config.AppConfig
        com.outcastgeek.util.Sessions
        com.outcastgeek.domain.Entities
        [clojurewerkz.quartzite.jobs :only [defjob]]
        [clojurewerkz.quartzite.schedule.simple :only [schedule repeat-forever with-interval-in-minutes]])
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :as j])
  (:import java.util.Date))

;;;;;;;;;;;;    Payroll Cycle Job ;;;;;;;;;;;;;;;;;;;;;;;;;

(defjob PayrollCycleCreator
  [ctx]
  (createCurrentPayrollCycle)
  (createCurrentTimesheets))

(defn schedulePayrollCycleCreator []
  (let [payrollCreatorJob (j/build
              (j/of-type PayrollCycleCreator)
              (j/with-identity (j/key payrollCreator)))
        payrollCreatorTrigger (t/build
                  (t/with-identity (t/key payrollTrigger))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (repeat-forever)
                                     (with-interval-in-minutes 1))))]
    (qs/schedule payrollCreatorJob payrollCreatorTrigger)))

;;;;;;;;;;;;    Sessions Job ;;;;;;;;;;;;;;;;;;;;;;;;;

(defjob SessionsCleaner
  [ctx]
  (cleanExpiredSessions))

(defn scheduleSessionsCleaner []
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

(defn runJobs []
  ;; starting Quartz Scheduler"""
  (qs/initialize)
  (qs/start)
  (scheduleSessionsCleaner)
  (schedulePayrollCycleCreator))
