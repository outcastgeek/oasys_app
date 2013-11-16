(ns oasysusa.profile.core
  (:require
            [oasysusa.utils.form :as validation]))

(def profile-state (atom {:ids ["first_name" "last_name" "email" "address" "telephone_number"]}))

; run
(validation/run profile-state)
