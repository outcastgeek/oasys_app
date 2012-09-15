(ns com.outcastgeek.services.web.Payroll
  (:use clojure.tools.logging
        clojure.pprint
        hiccup.page
        somnium.congomongo
        com.outcastgeek.services.web.fluid
        com.outcastgeek.config.AppConfig
        com.outcastgeek.domain.Entities
        com.outcastgeek.util.Macros
        [clj-style.core :as cs]
        com.outcastgeek.services.web.style))

(defn timesheets-controller [params session]
  (debug session))