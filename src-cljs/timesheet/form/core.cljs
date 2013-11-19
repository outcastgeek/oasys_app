(ns oasysusa.timesheet.form
  (:require
    [oasysusa.utils.form :as validation]))

(def project-state (atom {:ids ["description" "Hours1" "Hours2" "Hours3" "Hours4" "Hours5" "Hours6" "Hours7"]
;                          :datepicker_ids ["datepicker_iso_8601"]
;                          :datepicker_target_ids ["new_date"]
                          }))

; run
(validation/run project-state)
