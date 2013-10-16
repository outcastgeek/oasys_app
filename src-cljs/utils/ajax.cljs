(ns oasysusa.ajax
  (:require [goog.net.XhrIo :as xhr]))

(defn receiver [event callback]
  (let [response (.-target event)]
    (callback (.getResponseText response))))

(defn GET [url success-fn & error-fn]
  (let [success-callback (fn [event] (receiver event success-fn))
        error-callback (fn [event] (receiver event error-fn))]
    (xhr/send url success-callback)))

(defn POST [url content success-fn & error-fn]
  (let [success-callback (fn [event] (receiver event success-fn))
        error-callback (fn [event] (receiver event error-fn))]
    (xhr/send url "POST" success-callback content)))
