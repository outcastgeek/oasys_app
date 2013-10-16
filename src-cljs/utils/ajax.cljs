(ns oasysusa.ajax
  (:require [goog.net.XhrIo :as xhr]
            [cljs.core.async :as async :refer [chan close! >! <!]])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(defn receiver [event callback]
  (let [response (.-target event)]
    (callback (.getResponseText response))))

;(defn GET [url success-fn & error-fn]
;  (let [success-callback (fn [event] (receiver event success-fn))
;        error-callback (fn [event] (receiver event error-fn))]
;    (xhr/send url success-callback)))

(defn GET [url success-fn & error-fn
           ]
  (let [ch (chan 1)]
    (xhr/send url
      (fn [event]
        (let [res (-> event .-target .getResponseText)]
          (go (>! ch res)
            ;(close! ch)
            ))))
    (go
      (success-fn (<! ch))
      (close! ch)
      )
    ;ch
    ))

(defn POST [url content success-fn & error-fn]
  (let [success-callback (fn [event] (receiver event success-fn))
        error-callback (fn [event] (receiver event error-fn))]
    (xhr/send url "POST" success-callback content)))
