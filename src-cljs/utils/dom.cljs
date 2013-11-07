(ns oasysusa.utils.dom
;  (:use [jayq.core :only [$]])
  (:require [clojure.string :as cljstr]
;            [jayq.core :as jq]
;            [jayq.util :as jqutil]
            [domina :as dom]
            [domina.events :as ev]
            [domina.css :as css]
            [cljs.core.async :as async :refer [chan timeout close! >! <!]]
            [oasysusa.utils.ajax :as ajax])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(defn log [& msg]
  (go
    ;(apply jqutil/log msg)
    (apply dom/log msg)))

(defn evt->el [event]
  ;($ (.-target event))
  (ev/target event))

(defn by-id [id]
  ;($ (str "#" id))
  (dom/by-id id))

(defn value [el]
  ;(jq/val el)
  (dom/value el))

(defn set-value! [el val]
  ;(jq/val ($ el) val)
  (dom/set-value! el val))

(defn attr [el prop]
  ;(jq/attr ($ el) prop)
  (dom/attr el prop))

(defn set-attr! [el prop val]
  ;(jq/attr ($ el) prop val)
  (dom/set-attr! el prop val))

(defn prevent-default [evt]
  ;(.preventDefault evt)
  (ev/prevent-default evt))

(defn listen! [el event handler]
  ;(jq/bind el event handler)
  (ev/listen! el event handler))

(defn format-date [raw-date-string]
  (let [new-date (cljstr/join "/" (reverse (cljstr/split raw-date-string #"-")))]
    (log "Formatted Date: " raw-date-string " into Date: " new-date)
    new-date))
