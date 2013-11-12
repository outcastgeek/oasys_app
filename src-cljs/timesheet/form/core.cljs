(ns oasysusa.timesheet.form
  (:require
    [oasysusa.utils.form :as validation]))

(def project-state (atom {:ids ["description" "Hours1" "Hours2" "Hours3" "Hours4" "Hours5" "Hours6" "Hours7"]}))

; run
(validation/run project-state)
