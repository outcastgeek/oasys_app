(ns com.outcastgeek.services.web.WorkListener
  (:use clojure.tools.logging
;        com.outcastgeek.config.akka
        com.outcastgeek.services.web.Services)
  (:gen-class
   :implements [javax.servlet.ServletContextListener]))

(defn -contextDestroyed
  [this context]
  ;(debug "Shutting down actors...")
  ;(. aggregatorActor sendOneWay poison-pill)
  )

(defn -contextInitialized
  [this context]
  ;(runJob "900")
  )
