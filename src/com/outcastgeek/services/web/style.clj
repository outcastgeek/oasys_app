(ns com.outcastgeek.services.web.style
  (:use [clj-style.core :as cs]))

;(cs/defrule bogusStyleForName
;  [:p#name
;   :color "red"])

(cs/defmixin blue []
  :color :blue)

(cs/defmixin p-info []
  :color :black
  :font-style "italic"
  )

(cs/defmixin resume-section-title []
  :font-style "bold"
  :color :red
)

(cs/defmixin width-max []
  :width "100%"
)


; .resume-section-title h2 .......
;
; .resume-section-title p .....