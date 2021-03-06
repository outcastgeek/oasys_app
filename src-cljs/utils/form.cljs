(ns oasysusa.utils.form
  (:import goog.i18n.DateTimeFormat
           goog.i18n.DateTimeParse
           goog.ui.LabelInput
           goog.ui.InputDatePicker)
  (:require [clojure.string :as cljstr]
            [cljs.core.async :as async :refer [chan alts! timeout close! put! <!]]
            [oasysusa.utils.dom :as domutil]
            [oasysusa.utils.ajax :as ajax])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(defn run [form-state]
  (let [input_ids (get-in @form-state [:ids ])
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
                       id (domutil/attr target-input :id )
                       val (domutil/value target-input)]
                   (domutil/log "Profile Update KeyVal: " (domutil/attr target-input :id ) " => " (domutil/value target-input))
                   (if (= (domutil/attr target-input :input_type ) "date")
                     (swap! form-state update-in [:state ]
                       merge {(keyword id) (domutil/format-date val)})
                     (swap! form-state update-in [:state ]
                       merge {(keyword id) val}))
                   ;(domutil/log "Sending profile update...")
                   (update-inputs (get-in @form-state [:state ]))
                   ;(domutil/log @form-state)
                   ))

        grab-initial-value (fn [target-id]
                             ;(domutil/log "Grabbing initial value for: " target-id)
                             (let [el (domutil/by-id target-id)
                                   val (domutil/value el)]
                               (if (= (domutil/attr el :input_type ) "date")
                                 (swap! form-state update-in [:state ]
                                   merge {(keyword target-id) (domutil/format-date val)})
                                 (swap! form-state update-in [:state ]
                                   merge {(keyword target-id) val})))
                             target-id)

        clear (fn [evt input_ids form-state]
                (domutil/prevent-default evt)
                (doseq [x input_ids]
                  (swap! form-state update-in [:state ]
                    merge {(keyword x) nil}))
                (domutil/log "Cleared profile state: " @form-state)
                (doseq [x input_ids]
                  (update-input-value x nil)))
        render_datepicker (fn [parent_id target_id]
                            (let [parent_div (domutil/by-id parent_id)
                                  target_input (domutil/by-id target_id)
                                  pattern "MM/DD/YYYY"
                                  formatter (DateTimeFormat. pattern)
                                  parser (DateTimeParse. pattern)
                                  labelInput (doto (LabelInput. pattern)
                                               (.render target_input))
                                  datepicker (doto (InputDatePicker. formatter parser)
                                               (.setPopupParentElement parent_div)
                                               (.decorate (.getElement labelInput)))]
;                              (.decorate datepicker (.getElement labelInput))
                               (.enterDocument datepicker)
                              (domutil/listen! (domutil/by-id target_id) :focus
                                (fn [event] (domutil/log (.getDate datepicker))))
                              (domutil/listen! (domutil/by-id target_id) :blur
                                (fn [event] (domutil/log (.getDate datepicker))))
                              ))
;        datepicker_ids (get-in @form-state [:datepicker_ids ])
;        datepicker_target_ids (get-in @form-state [:datepicker_target_ids ])
        ]
;    (doseq [[dpid dptid] (map list datepicker_ids datepicker_target_ids)]
;      (render_datepicker dpid dptid))
    (doseq [x input_ids]
      (grab-initial-value x))
    (update-inputs (get-in @form-state [:state ]))
    (doseq [x input_ids]
      (domutil/listen! (domutil/by-id x) :blur update)
      (domutil/listen! (domutil/by-id x) :change update)
      )
    (domutil/listen! (domutil/by-id "clear") :click (fn [evt]
                                                      (clear evt input_ids form-state)))
    ))
