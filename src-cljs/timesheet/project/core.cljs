(ns oasysusa.timesheet.project
  (:require
    [oasysusa.utils.form :as validation]))

(def project-state (atom {:ids ["name" "client" "description" "email" "telephone_number" "address" "manager" "manager_telephone_number" "manager_email"]}))

; run
(validation/run project-state)
