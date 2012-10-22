(ns com.outcastgeek.util.Mail
  (:use clojure.tools.logging
        postal.core
        okku.core
        com.outcastgeek.config.AppConfig)
  (:require [clj-http.client :as client]))

(defn sendWelcomeEmail [employeeData]
  (debug "Sending welcome email to " (employeeData :username) "here" (employeeData :email))
  (send-message ^{:user smtpUser :pass smtpPwd
                  :host smtpHost
                  :port smtpPort}
                 {:from smtpSender
                  ; Make sure to request production access to send emails to actual target
                  :to (employeeData :email)
                  ;:to smtpSender
                  :subject "Your WebApp Profile has been created."
                  :body (str "Welcome to " appName " " (employeeData :username) "!!!!")}))

(def mail-actor
  (spawn
    (actor (onReceive [msg]
                      (sendWelcomeEmail msg)))
    :name "sendWelcomeEmail"
    :in actorSystem))

(defn queueSendWelcomeEmail [data]
  (.tell mail-actor data))
