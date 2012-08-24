(ns com.outcastgeek.domain.Entities
  (:use clojure.tools.logging
        com.outcastgeek.config.AppConfig
        com.outcastgeek.util.Mail
        korma.core
        korma.db)
  (:require [resque-clojure.core :as resque]
            [clj-time.core :as time])
  (:import ;com.outcastgeek.domain.Employees
           java.util.Date))

;(def employeesEntity
;  (. appCtx getBean "employees"))

;;;; Check out: https://gist.github.com/2e8a3d55d80707ce79e0

;;;;;;;;;;;;;;;;; DATABASE ;;;;;;;;;;;;;;;;;;;;;;;

(defdb db {:classname (appProperties :class-name)
           :subprotocol (appProperties :sub-protocol)
           :user (appProperties :username)
           :password (appProperties :passwd)
           :subname (appProperties :sub-name)})

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
                         [:username :first_name :last_name :email :password])
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

(defn queueEmployeeCreation [data]
  (resque/enqueue employeeQueue
                    "com.outcastgeek.domain.Entities/createEmployee"
                    data))

(defn queueEmployeeUpdate [data]
  (resque/enqueue employeeQueue
                    "com.outcastgeek.domain.Entities/updateEmployee"
                    data))

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
          (where (or (like :payroll_cycle_year (data :payroll_cycle_year))
                     (like :payroll_cycle_number (data :payroll_cycle_number))))))

(defn createCurrentPayrollCycle []
  (debug "Checking / Creating this month's payroll cycle...")
  (let [rightNow (time/now)
        lastDayOfTheMonth (lastDayOfTheMonthOf rightNow)
        lastDayOfTheFollowingMonth (lastDayOfTheMonthOf (time/plus rightNow (time/months 1)))
        payrollData {:payroll_cycle_year (time/year rightNow)
                     :payroll_cycle_number (time/month rightNow)
                     :start_date (firstDayOfTheMonthOf rightNow)
                     :end_date (lastDayOfTheMonthOf rightNow)
                     :direct_deposit_date lastDayOfTheFollowingMonth
                     :check_date lastDayOfTheFollowingMonth
                     :created_at (get-current-timestamp)
                     :updated_at (get-current-timestamp)}
        existingPayroll (first (findExistingPayroll payrollData))]
    (when
      (nil? existingPayroll)
      (debug "PERSISTING NEW PAYROLL: " payrollData)
      (insert payroll_cycles
              (values payrollData))
      )))

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

