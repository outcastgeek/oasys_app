(ns com.outcastgeek.domain.User
  (:use com.outcastgeek.config.AppConfig)
  (:require [clojure.java.jdbc :as sql]
            clj-record.boot)
  (:import java.util.Date
           java.sql.Timestamp))

(defn get-current-timestamp []
  (Timestamp. (. (Date.) getTime)))

(clj-record.core/init-model
  :table-name "employees")


;(defn print-record [record]
;  (debug "record: " record)
;  record)
;
;(clj-record.core/init-model
;  :table-name "employees"
;  (:callbacks
;    (:before-save :created_at set-timestamp)
;    ;(:before-save :updated_at set-timestamp)
;    (:before-update :updated_at set-timestamp)))
