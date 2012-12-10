(ns com.outcastgeek.domain.Entities
  (:use clojure.tools.logging
        com.outcastgeek.config.AppConfig
        com.outcastgeek.util.Mail
        korma.core
        korma.db
        clj-time.coerce)
  (:require [clojure.core.reducers :as r]
            [clj-time.core :as time])
  (:import java.util.Date))

;;;; Check out: https://gist.github.com/2e8a3d55d80707ce79e0

;;;;;;;;;;;;;;;;; DATABASE ;;;;;;;;;;;;;;;;;;;;;;;

(defdb db {:classname (appProperties :class-name)
           :subprotocol (appProperties :sub-protocol)
           :user (appProperties :username)
           :password (appProperties :passwd)
           :subname (appProperties :sub-name)})

(defn sqlCast [x as]
  (raw (format "CAST(%s AS %s)" (name x) (name as))))

;;;;;;;;;;;;;;;   ENTITIES     ;;;;;;;;;;;;;;;;;;;;;;;;;

(defentity employees)

(defentity payroll_cycles)

(defentity projects)

(defentity time_sheets)

(defentity work_segments)

;;;;;;;;;;;;;;;;;;     Employees       ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn allEmployees []
  (select employees))

(defn updateEmployee [data]
  (let [employeeData (merge
                       (select-keys
                         data
                         [:uniq :username :first_name :last_name :email :password])
                       {:updated_at (get-current-timestamp)})]
    (debug "Updating employee with: " employeeData)
    (update employees
          (set-fields employeeData)
          (where {:uniq (employeeData :uniq)}))))

(defn findEmployee [data]
  (debug "EMPLOYEE CRITERIA: " data)
  (select employees
          (where data)))

(defn findExistingEmployee [data]
  (debug "FINDING EMPLOYEE CRITERIA: " data)
  (select employees
          (where (or (like :uniq (data :uniq))
                     (like :username (data :username))))))

(defn createEmployee [data]
  (debug "RAW EMPLOYEE DATA: " data)
  (let [employeeData (merge
                        (select-keys
                          data
                          [:username :first_name :last_name :email :password :uniq :provider])
                        {:active false
                         :created_at (get-current-timestamp)
                         :updated_at (get-current-timestamp)})
        existingEmployee (first (findExistingEmployee data))]
    (when
      (nil? existingEmployee)
      (debug "PERSISTING NEW EMPLOYEE: " employeeData)
      (insert employees
              (values employeeData))
      (queueSendWelcomeEmail data)
      )))

;;;;;;;;;;;;;;;;;;     Payroll Cycles       ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn allPayrollCycles []
  (select payroll_cycles))

(defn findPayrollCycle [data]
  (debug "PAYROLL CYCLE CRITERIA: " data)
  (select payroll_cycles
          (where data)))

(defn findExistingPayroll [data]
  (debug "FINDING PAYROLL CRITERIA: " data)
  (select payroll_cycles
          (where (or (= :payroll_cycle_year (data :payroll_cycle_year))
                     (= :payroll_cycle_number (data :payroll_cycle_number)))
                 )))

(defn createCurrentPayrollCycle []
  (debug "Checking / Creating this month's payroll cycle...")
  (let [rightNow (time/now)
        lastDayOfTheMonth (lastDayOfTheMonthOf rightNow)
        lastDayOfTheFollowingMonth (lastDayOfTheMonthOf (time/plus rightNow (time/months 1)))
        payrollData {:payroll_cycle_year (time/year rightNow)
                     :payroll_cycle_number (time/month rightNow)
                     :start_date (to-sql-date (firstDayOfTheMonthOf rightNow))
                     :end_date (to-sql-date (lastDayOfTheMonthOf rightNow))
                     :direct_deposit_date (to-sql-date lastDayOfTheFollowingMonth)
                     :check_date (to-sql-date lastDayOfTheFollowingMonth)
                     :created_at (get-current-timestamp)
                     :updated_at (get-current-timestamp)}
        existingPayroll (first (findExistingPayroll payrollData))]
    (when
      (nil? existingPayroll)
      (debug "PERSISTING NEW PAYROLL: " payrollData)
      (insert payroll_cycles
              (values payrollData))
      )))

;;;;;;;;;;;;;;;;;;     Timesheets       ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn allTimesheets []
  (select time_sheets))

(defn findTimesheet [data]
  (debug "TIMESHEET CRITERIA: " data)
  (select time_sheets
          (where data)))

(defn findEmployeeCurrentTimesheet [data]
  (debug "TIMESHEET CRITERIA: " data)
  (select time_sheets
          (where (and (= :employee_id (data :employee_id))
                      (= :start_date (data :start_date))
                      (= :end_date (data :end_date)))
                 )))

(defn findExistingTimesheet [data]
  (debug "TIMESHEET CRITERIA: " data)
  (select time_sheets
          (where (and (= :start_date (data :start_date))
                      (= :end_date (data :end_date)))
                 )))



(defn createCurrentTimesheets []
  (debug "Checking / Creating this week's employees timesheets...")
  (let [rightNow (time/now)
        payrollCycle (first (findExistingPayroll {:payroll_cycle_year (time/year rightNow)
                                                  :payroll_cycle_number (time/month rightNow)}))
        timesheetData {:start_date (to-sql-date (firstDayOfTheWeekOf rightNow))
                       :end_date (to-sql-date (lastDayOfTheWeekOf rightNow))
                       :created_at (get-current-timestamp)
                       :updated_at (get-current-timestamp)}
        existingTimesheet (first (findExistingTimesheet timesheetData))
        newTimesheetForEmployee (fn [employee]
                                  (let [data (merge timesheetData
                                                    {:payroll_cycle_id (payrollCycle :id)
                                                     :employee_id (employee :id)})]
                                    (debug "PERSISTING NEW TIMESHEET FOR EMPLOYEE: " employee)
                                    (debug "AND WITH DATA: " data)
                                    (insert time_sheets
                                            (values data))))]
    (when
      (and
        (nil? existingTimesheet)
        (not (nil? payrollCycle)))
      (doall
        (pmap newTimesheetForEmployee (allEmployees)))
      )))

;;;;;;;;;;;;;;;;;;     WorkSegments   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn findWorksegment [data]
  (debug "WORKSEGMENT CRITERIA: " data)
  (select work_segments
          (where data)))

;;;;;;;;;;;;;;;;;;     Projects       ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn allProjects []
  (select projects))

(defn findProject [data]
  (debug "PROJECT CRITERIA: " data)
  (select projects
          (where data)))

(defn findExistingProject [data]
  (debug "FINDING EMPLOYEE CRITERIA: " data)
  (select projects
          (where (like :name (data :name)))))

(defn updateProject [data]
  (let [projectData (merge
                        (select-keys
                          data
                          [:name :client :description])
                        {:updated_at (get-current-timestamp)})]
    (update projects
            (set-fields projectData)
            (where (like :uniq (projectData :name))))
    ))

(defn upsertProject [data]
  (debug "RAW PROJECT DATA: " data)
  (let [projectData (merge
                        (select-keys
                          data
                          [:name :client :description])
                        {:created_at (get-current-timestamp)
                         :updated_at (get-current-timestamp)})
        existingProject (first (findExistingProject data))]
    (if
      (nil? existingProject)
      (do
        (debug "PERSISTING NEW PROJECT: " projectData)
      (insert projects
              (values projectData)))
      (updateProject projectData))))

(defn queueEmployeeCreation [data]
  (future (createEmployee data)))

(defn queueEmployeeUpdate [data]
  (future (updateEmployee data)))
