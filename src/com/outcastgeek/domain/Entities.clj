(ns com.outcastgeek.domain.Entities
  (:use clojure.tools.logging
        com.outcastgeek.config.AppConfig
        com.outcastgeek.services.web.Mail
        korma.core
        korma.db)
  (:require [resque-clojure.core :as resque])
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn allEmployees []
  (select employees))

(defn updateEmployee [data]
  (update employees
          (set-fields (select-keys
                        data
                        [:username :first_name :last_name :email :password]))
          (where {:uniq (data :uniq)})))

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
;      (with-transaction
;        (doto (Employees.)
;	        (.setFirstName (employeeData :first_name))
;	        (.setLastName (employeeData :last_name))
;	        (.setUsername (employeeData :username))
;	        (.setEmail (employeeData :email))
;	        (.setActive false)
;	        (.setuniq (employeeData :uniq))
;	        (.setProvider (employeeData :provider))
;	        (.setCreatedAt (Date.))
;	        (.setUpdatedAt (Date.))
;          (.persist)
;          (.flush)))
      (sendWelcomeEmail employeeData)
      )))
