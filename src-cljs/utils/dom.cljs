(ns oasysusa.utils.dom
  (:require [clojure.string :as cljstr]
            [domina :as dom]
            [domina.events :as ev]
            [domina.css :as css]
            [cljs.core.async :as async :refer [chan timeout close! >! <!]]
            [oasysusa.utils.ajax :as ajax])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(defn log [& msg]
  (go
    (apply dom/log msg)))

(defn evt->el [event]
  (css/sel (ev/target event)))

(defn format-date [raw-date-string]
  (let [new-date (cljstr/join "/" (reverse (cljstr/split raw-date-string #"-")))]
    (log "Formatted Date: " raw-date-string " into Date: " new-date)
    new-date))
