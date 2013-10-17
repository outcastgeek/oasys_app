(ns oasysusa.profile.core
  (:require [clojure.string :as cljstr]
            [domina :as dom]
            [domina.events :as ev]
            [domina.css :as css]
            [cljs.core.async :as async :refer [chan alts! timeout close! put! <!]]
            [oasysusa.utils.dom :as domutil]
            [oasysusa.utils.ajax :as ajax])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(def profile-state (atom {:ids ["first_name" "last_name" "email" "date_of_birth" "address" "telephone_number"]}))

(defn update-input-value [id val]
  (domutil/log "View Update KeyVal: " id " => " val)
  (let [el (dom/by-id id)
        attrs (dom/attrs el)]
    (dom/set-value! (dom/by-id id) val)
    (if (and
          (= (attrs :required ) "required")
          (cljstr/blank? val))
      (dom/set-attr! el :class "invalid dirty")
      (dom/set-attr! el :class "valid dirty"))
    ))

(defn update-inputs [new-profile]
  (domutil/log "Handling profile change: " new-profile)
  (doseq [[k v] new-profile]
    (go
      (update-input-value (name k) v))))

(defn update [event]
  (let [target-input (domutil/evt->el event)
        attrs (dom/attrs target-input)
        id (dom/attr target-input "id")
        val (dom/value target-input)]
    (domutil/log "Profile Update KeyVal: " (dom/attr target-input "id") " => " (dom/value target-input))
    (if (= (attrs :input_type ) "date")
      (swap! profile-state update-in [:profile ]
        merge {(keyword id) (domutil/format-date val)})
      (swap! profile-state update-in [:profile ]
        merge {(keyword id) val}))
    (domutil/log "Sending profile update...")
    (update-inputs (get-in @profile-state [:profile ]))
    ;(domutil/log @profile-state)
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
  (domutil/log "Grabbing initial value for: " target-id)
  (let [el (dom/by-id target-id)
        attrs (dom/attrs el)
        val (dom/value el)]
    (if (= (attrs :input_type ) "date")
      (swap! profile-state update-in [:profile ]
        merge {(keyword target-id) (domutil/format-date val)})
      (swap! profile-state update-in [:profile ]
        merge {(keyword target-id) val})))
  target-id)

(defn clear [evt input_ids]
  (ev/prevent-default evt)
  (let [input_ids (get-in @profile-state [:ids ])]
    (doseq [x input_ids]
      (swap! profile-state update-in [:profile ]
        merge {(keyword x) nil}))
    (domutil/log "Cleared profile state: " @profile-state)
    (update-inputs (get-in @profile-state [:profile ]))
    ))

(defn run [profile-state]
  (let [input_ids (get-in @profile-state [:ids ])]
;    (go
;      (domutil/log "Ajax GET started")
;      (let [initial-profile (js->clj (<! (ajax/GET "/employee")))]
;        (swap! profile-state update-in [:profile ]
;          merge initial-profile)
;        (domutil/log "Sending profile update...")
;        (domutil/log "Ajax GET completed...")
;        ;(<! (timeout 1000))
;        (domutil/log "Fetched initial state: " initial-profile)
;        (domutil/log "Initial Profile State: " @profile-state)
;        (update-inputs (get-in @profile-state [:profile ]))))
    (doseq [x input_ids]
      (grab-initial-value x))
    (update-inputs (get-in @profile-state [:profile ]))
    (doseq [x input_ids]
      (attach-blur-listener x))
    (ev/listen! (dom/by-id "clear") :click (fn [evt]
                                             (clear evt input_ids)))
    ))

; run
(run profile-state)
