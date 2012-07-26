(ns com.outcastgeek.domain.Entities
  (:use clojure.tools.logging
        com.outcastgeek.config.AppConfig
        korma.core
        korma.db))

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

(defn findEmployee [data]
  (debug "EMPLOYEE CRITERIA: " data)
  (select employees
          (where data)))

(defn createEmployee [data]
  (debug "RAW EMPLOYEE DATA: " data)
  (let [employeeData (merge
                        (select-keys
                          data
                          [:username :first_name :last_name :email :unique :provider])
                        {:active false
                         :created_at (get-current-timestamp)
                         :updated_at (get-current-timestamp)})]
    (when
      (empty? (findEmployee {:unique (employeeData :unique)}))
      (debug "PERSISTING NEW EMPLOYEE: " employeeData)
      (insert employees
              (values employeeData)))
    ))
