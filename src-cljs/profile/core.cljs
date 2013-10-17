(ns oasysusa.profile.core
  (:require [clojure.string :as cljstr]
            [cljs.core.async :as async :refer [chan alts! timeout close! put! <!]]
            [oasysusa.utils.dom :as domutil]
            [oasysusa.utils.ajax :as ajax])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(defn run []
  (let [profile-state (atom {:ids ["first_name" "last_name" "email" "date_of_birth" "address" "telephone_number"]})
        input_ids (get-in @profile-state [:ids ])
        update-input-value (fn [id val]
                             (domutil/log "View Update KeyVal: " id " => " val)
                             (let [el (domutil/by-id id)]
                               (when-not (nil? el)
                                 (domutil/set-value! el val)
                                 (if (and
                                       (= (domutil/attr el :required ) "required")
                                       (cljstr/blank? val))
                                   (domutil/set-attr! el :class "invalid dirty")
                                   (domutil/set-attr! el :class "valid dirty")))
                               ))

        update-inputs (fn [new-profile]
                        ;(domutil/log "Handling profile change: " new-profile)
                        (doseq [[k v] new-profile]
                          (update-input-value (name k) v)))

        update (fn [event]
                 (let [target-input (domutil/evt->el event)
                       id (domutil/attr target-input "id")
                       val (domutil/value target-input)]
                   (domutil/log "Profile Update KeyVal: " (domutil/attr target-input "id") " => " (domutil/value target-input))
                   (if (= (domutil/attr target-input :input_type ) "date")
                     (swap! profile-state update-in [:profile ]
                       merge {(keyword id) (domutil/format-date val)})
                     (swap! profile-state update-in [:profile ]
                       merge {(keyword id) val}))
                   ;(domutil/log "Sending profile update...")
                   (update-inputs (get-in @profile-state [:profile ]))
                   ;(domutil/log @profile-state)
                   ))

        grab-initial-value (fn [target-id]
                             ;(domutil/log "Grabbing initial value for: " target-id)
                             (let [el (domutil/by-id target-id)
                                   val (domutil/value el)]
                               (if (= (domutil/attr el :input_type ) "date")
                                 (swap! profile-state update-in [:profile ]
                                   merge {(keyword target-id) (domutil/format-date val)})
                                 (swap! profile-state update-in [:profile ]
                                   merge {(keyword target-id) val})))
                             target-id)

        clear (fn [evt input_ids]
                (domutil/prevent-default evt)
                (let [input_ids (get-in @profile-state [:ids ])]
                  (doseq [x input_ids]
                    (swap! profile-state update-in [:profile ]
                      merge {(keyword x) nil}))
                  (domutil/log "Cleared profile state: " @profile-state)
                  (update-inputs (get-in @profile-state [:profile ]))
                  ))]
    (doseq [x input_ids]
      (grab-initial-value x))
    (update-inputs (get-in @profile-state [:profile ]))
    (doseq [x input_ids]
      (domutil/listen! (domutil/by-id x) :blur update)
      ;(attach-blur-listener x)
      )
    (domutil/listen! (domutil/by-id "clear") :click (fn [evt]
                                                      (clear evt input_ids)))
    ))

; run
(run)
