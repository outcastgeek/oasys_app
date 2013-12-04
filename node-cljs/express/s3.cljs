(ns express.s3
  (:require [cljs.nodejs :as node]))

(def zmq (node/require "zmq"))
(def responder (.socket zmq "rep"))

(defn on-msg [raw-msg]
  (let [msg (js->clj raw-msg)]
    (.log js/console (str "Received request: [" msg "]"))
    (.send responder "world")))

(defn on-connection-error [err]
  (if err
    (.log js/console err)
    (.log js/console "Listening on 5555...")))

(defn on-sigint []
  (.close responder))

(.on responder "message" on-msg)
(.bind responder "tcp://127.0.0.1:5555" on-connection-error)
;(.on process "SIGINT" on-sigint)
