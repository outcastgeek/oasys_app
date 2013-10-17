(ns oasysusa.timesheet.project
  (:require
    [oasysusa.utils.form :as validation]))

(def project-state (atom {:ids ["name" "client" "description" "email" "telephone_number" "address"]}))

; run
(validation/run project-state)
