(ns oasysusa.utils.dom
  (:use [jayq.core :only [$]])
  (:require [clojure.string :as cljstr]
            [jayq.core :as jq]
            [jayq.util :as jqutil]
            [cljs.core.async :as async :refer [chan timeout close! >! <!]]
            [oasysusa.utils.ajax :as ajax])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(defn log [& msg]
  (go
    (apply jqutil/log msg)))

(defn evt->el [event]
  ($ (.-target event)))

(defn by-id [id]
  ($ (str "#" id)))

(defn value [el]
  (jq/val el))

(defn set-value! [el val]
  (jq/html ($ el) val))

(defn attr [el prop]
  (jq/attr ($ el) prop))

(defn set-attr! [el prop val]
  (jq/attr ($ el) prop val))

(defn listen! [el event handler]
  (jq/bind el event handler))

(defn format-date [raw-date-string]
  (let [new-date (cljstr/join "/" (reverse (cljstr/split raw-date-string #"-")))]
    (log "Formatted Date: " raw-date-string " into Date: " new-date)
    new-date))
