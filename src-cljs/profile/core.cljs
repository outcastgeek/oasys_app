(ns oasysusa.profile.core
  (:require [clojure.string :as cljstr]
            [domina :as dom]
            [domina.events :as ev]
            [domina.css :as css]
            [cljs.core.async :as async :refer [chan close! >! <!]]
            [oasysusa.ajax :as ajax])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(def profile-state (atom {:ids ["first_name" "last_name" "email" "date_of_birth" "address" "telephone_number"]}))

(defn evt->el [event]
  (css/sel (ev/target event)))

(defn format-date [raw-date-string]
  (let [new-date (cljstr/join "/" (reverse (cljstr/split raw-date-string #"-")))]
    ;(dom/log "Formatted Date: " raw-date-string " into Date: " new-date)
    new-date))

(defn update [event]
  (let [target-input (evt->el event)
        attrs (dom/attrs target-input)
        id (dom/attr target-input "id")
        val (dom/value target-input)]
    ;(dom/log "Profile Update KeyVal: " (dom/attr target-input "id") " => " (dom/value target-input))
    (if (= (attrs :input_type) "date")
      (swap! profile-state update-in [:profile]
        merge {(keyword id) (format-date val)})
      (swap! profile-state update-in [:profile]
        merge {(keyword id) val}))
    ;(dom/log @profile-state)
    ))

(defn update-input-value [id val]
  (let [el (dom/by-id id)
        attrs (dom/attrs el)]
    ;(dom/log "View Update KeyVal: " id " => " val)
    (dom/set-value! (dom/by-id id) val)
    (if (and
          (= (attrs :required) "required")
          (cljstr/blank? val))
      (dom/set-attr! el :class "invalid dirty")
      (dom/set-attr! el :class "valid dirty"))
    ))

(defn attach-blur-listener [target-id]
  (ev/listen! (dom/by-id target-id) :blur update)
  target-id)

(defn attach-change-listener [target-id]
  (ev/listen! (dom/by-id target-id) :change update)
  target-id)

(defn attach-keydown-listener [target-id]
  (ev/listen! (dom/by-id target-id) :keypress update)
  target-id)

(defn grab-initial-value [target-id]
  (let [el (dom/by-id target-id)
        attrs (dom/attrs el)
        val (dom/value el)]
    (if (= (attrs :input_type) "date")
      (swap! profile-state update-in [:profile]
        merge {(keyword target-id) (format-date val)})
      (swap! profile-state update-in [:profile]
        merge {(keyword target-id) val})))
  target-id)

(defn clear [evt]
  (ev/prevent-default evt)
  (let [input_ids (get-in @profile-state [:ids])]
    (dorun
      (for [x input_ids]
        (swap! profile-state update-in [:profile]
          merge {(keyword x) nil})))
    (dom/log "Cleared profile state: " @profile-state)
    ))

(defn initialize-profile-state [state-json]
  (let [initial-profile-state (js->clj state-json)]
;    (dom/log "Fetched initial state: " state-json)
    (swap! profile-state update-in [:profile]
      merge initial-profile-state)
    (dom/log "Fetched initial state: " @profile-state)
    ))

(defn bootstrap []
  (add-watch profile-state :profile
    (fn [_ _ old new]
      (let [new-profile (get-in @profile-state [:profile])]
        (dorun
          (for [[k v] new-profile]
            (update-input-value (name k) v)))
        )))
;  (go
;    (initialize-profile-state (<! (ajax/GET "/employee"))))
  (ajax/GET "/employee" initialize-profile-state)
  (let [input_ids (get-in @profile-state [:ids])]
    (dorun
      (for [x input_ids]
        (doto x
          grab-initial-value
;          attach-keydown-listener
;          attach-change-listener
          attach-blur-listener)))
    (ev/listen! (dom/by-id "clear") :click clear)
    (dom/log "Initial Profile State: " @profile-state)))

;; bootstrap
(bootstrap)
