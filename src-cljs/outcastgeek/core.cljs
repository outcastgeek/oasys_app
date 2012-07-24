(ns outcastgeek.core
  (:require [og.notepad :as notepad]))

;####################################################
; Console Logger
;####################################################

(defn ^:export customLog [msg]
  (.log js/console (pr-str msg)))

;####################################################
; End Console Logger
;####################################################
