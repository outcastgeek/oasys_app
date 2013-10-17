(ns oasysusa.utils.ajax
  (:require [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan timeout close! put! <!]])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

;(defn receiver [event callback]
;  (let [response (.-target event)]
;    (callback (.getResponseText response))))

;(defn GET [url success-fn & error-fn]
;  (let [success-callback (fn [event] (receiver event success-fn))
;        error-callback (fn [event] (receiver event error-fn))]
;    (xhr/send url success-callback)))

(defn receiver [event ch]
  (let [response (.-target event)]
    (go
      (put! ch (.getResponseText response)))))

(defn GET [url ;success-fn
           ]
  (let [ch (chan 1)]
    (xhr/send url
      (fn [event]
        (receiver event ch)))
    ch))

(defn POST [url content success-fn & error-fn]
  (let [success-callback (fn [event] (receiver event success-fn))
        error-callback (fn [event] (receiver event error-fn))]
    (xhr/send url "POST" success-callback content)))
