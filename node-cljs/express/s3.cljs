(ns express.s3
  (:require [cljs.nodejs :as node]
            ;[cljs.core.async :as async :refer [chan timeout close! put! <!]]
            ))

(def zmq (node/require "zmq"))
(def responder (.socket zmq "rep"))

;(defn on-sigint []
;  (.close responder))

(defn handle-msgs [responder]
  (let [on-msg (fn [raw-msg]
                 (let [msg (js->clj raw-msg)]
                   (.log js/console (str "Received request: [" msg "]"))
                   (.send responder "world")))]
    (.on responder "message" on-msg)))

(defn bind [responder address]
  (let [on-connection-error (fn [err]
                              (if err
                                (.log js/console err)
                                (.log js/console (str "Listening " address " ..."))))]
    (.bind responder address on-connection-error)))

(doto responder
  (handle-msgs)
  (bind "tcp://127.0.0.1:5555"))

;(.on process "SIGINT" on-sigint)
