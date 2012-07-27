(ns com.outcastgeek.services.web.Mail
  (:use clojure.tools.logging
        [cheshire.core :as json]
        com.outcastgeek.config.AppConfig)
  (:require [clj-http.client :as client]))

(defn sendWelcomeEmail [employeeData]
  (debug "Sending welcome email to " (employeeData :username) "here" (employeeData :email))
  (let [resp (client/post (appProperties :mail-send-host)
               {:query-params {:apikey (appProperties :mail-key)}}
               {:body (json/generate-string {:apikey (appProperties :mail-key)
                                             :text (str "Welcome to WebApp " (employeeData :username) "!!!!")
                                             :from_email "outcastgeek@gmail.com"
                                             :from_name "outcastgeek"
                                             :to_name "lambert"
                                             :to_email [(employeeData :email)
                                                        "outcastgeek@gmail.com"]
                                             :subject "Your WebApp Profile has been created."
                                             :track_opens true
                                             :track_clicks true
                                             :tags ["New Employee"
                                                    "WelcomeEmail"]})
                :content-type :json
                :socket-timeout 4000
                :conn-timeout 4000
                :accept :json
                :debug true
                :debug-body true})]
    (debug "MAIL SEND CALLBACK: " resp)))

