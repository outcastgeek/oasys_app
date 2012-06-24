(ns outcastgeek.core
  (:use [jayq.core :only [$ delegate prevent bind unbind remove-class fade-out]]
        [jayq.util :only [wait log clj->js]]))

;####################################################
; Handle dropdown menus
;####################################################

(defn dropdowns []
  (let [selectors ["a.menu", ".dropdown-toggle",
                   "[data-dropdown] a.menu",
                   "[data-dropdown] .dropdown-toggle"]
        collapse-menu (fn [e]
                        (prevent e)
                        (let [$li ($ e/currentTarget)]
                          (wait 6000 #((remove-class $li "open") false))))
        toggle-class (fn [selector]
                       (let [$selector ($ selector)]
                         (delegate $selector $selector :click
                           (fn [el]
                             (prevent el)
                             (let [$li (. $selector parent "li")
                                   isActive (. $li hasClass "open")]
                               (when-not isActive
                                 (. $li toggleClass "open")
                                 (bind $li :mouseout collapse-menu))
                               )))))]
    (doall
      (map toggle-class selectors))))

(dropdowns)

;####################################################
; Handle flash messages
;####################################################

(defn clean-flash-on-click [flash]
  (let [$flash ($ flash)
        clean (fn [e]
                (when e
                  (prevent e))
                (fade-out $flash 1000))]
    (delegate $flash $flash :click clean)
    (wait 6000 clean)))

(clean-flash-on-click "#flash")

;####################################################
; Handle alerts
;####################################################


