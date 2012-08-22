(ns com.outcastgeek.util.Mail
  (:use clojure.tools.logging
        postal.core
        com.outcastgeek.config.AppConfig)
  (:require [clj-http.client :as client]
            [resque-clojure.core :as resque]))

(defn sendWelcomeEmail [employeeData]
  (debug "Sending welcome email to " (employeeData :username) "here" (employeeData :email))
  (send-message ^{:user smtpUser :pass smtpPwd
                  :host smtpHost
                  :port smtpPort}
                 {:from smtpSender
                  ; Make sure to request production access to send emails to actual target
                  :to (employeeData :email)
;                  :to smtpSender
                  :subject "Your WebApp Profile has been created."
                  :body (str "Welcome to " appName " " (employeeData :username) "!!!!")}))

(defn queueSendWelcomeEmail [data]
  (resque/enqueue mailQueue
                    "com.outcastgeek.util.Mail/sendWelcomeEmail"
                    data))


