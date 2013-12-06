(ns utils.zmq
  (:use clojure.tools.logging)
  (:require [clojure.core.async :as async :refer [chan close! go >! <!! alts!!]]
            [cheshire.core :as json]
            [utils.zhelpers :as mq])
  (import [java.util.concurrent Executors ThreadFactory ScheduledThreadPoolExecutor TimeUnit]))

(def numberOfCpus
  (.availableProcessors (Runtime/getRuntime)))

(def reqrep-pool
  (Executors/newFixedThreadPool numberOfCpus))

; http://blog.zololabs.com/2013/08/17/running-a-clojure-function-periodically/

(defn- random-thread-name [prefix]
  (str prefix "-" (rand-int numberOfCpus)))
 
(defn thread-factory [thread-name-prefix]
  (proxy [ThreadFactory] []
    (newThread [thunk]
      (Thread. thunk (random-thread-name thread-name-prefix)))))
 
(defn scheduled-executor [pool-size thread-name-prefix]
  (->> thread-name-prefix
       thread-factory
       (ScheduledThreadPoolExecutor. pool-size)))
 
(defonce REQREP-EXECUTOR (scheduled-executor numberOfCpus
                                                "zmq-services-thread-pool"))

(def not-nil? (complement nil?))

;; The following was borrowed from: https://github.com/fogus/ring-edn/blob/master/src/ring/middleware/edn.clj
(defprotocol EdnRead
  (-read-edn [this]))

(extend-type String
  EdnRead
  (-read-edn [s]
    (read-string s)))

(extend-type java.io.InputStream
  EdnRead
  (-read-edn [is]
    (read (java.io.PushbackReader.
           (java.io.InputStreamReader.
             is "UTF-8"))
          false
          nil)))

(defn readMsg [msg]
;  (-read-edn msg)
  (json/decode msg))

(defn writeMsg [msg]
;  (pr-str msg)
  (json/decode msg))

(defprotocol Socket
  (configure [this])
  (->in [this msg])
  (<-out [this])
  (close [this]))

(defn applyAsync [func inputs & params]
  (let [channels (repeatedly (count inputs) chan)]
    (doseq [[channel item] (map list channels inputs)]
      (go (>! channel item)))
    (while true
      (let [[value chan] (alts!! channels)]
        (go (apply func value params))
        ))
    ))

(defn check-arg [arg]
  (when (nil? arg)
    (throw (IllegalArgumentException. "invalid argument(s) provided!!!!"))))

(defn connect [address sock]
  (debug "\nConnecting to: " address)
      (doto sock
        (.setSendTimeOut 1000)
        (.setReceiveTimeOut 1000)
        (.connect address)))

; Inspired by https://github.com/lynaghk/zmq-async/blob/master/src/com/keminglabs/zmq_async/core.clj

(deftype Client [ctx in out type addresses]
  Socket
  (configure [this]
    (map check-arg [ctx in out type addresses])
    (let [sock (mq/socket ctx (case type
                                :req mq/req
                                :xrep mq/xreq
                                :pair mq/pair
                                :pub mq/pub))]
  
      (pmap (fn [address] (connect address sock)) addresses)
      
      (go
       (loop [msg (<! in)]
        ;(debug msg)
        (mq/send sock (writeMsg msg))
        (try
          (>! out (readMsg (mq/recv-str sock)))
          (catch Exception e
            (error "Can't retrieve message " e)
            (.close sock)
            (go (configure this))
            ))
        (recur (<! in))))
      ))
  (->in [this msg]
    (go
     ;(debug "->in" msg)
     (>! in msg)))
  (<-out [this]
    ;(debug "<-out")
    (<!! out))
  (close [this]
    (close! in)
    (close! out)))

(deftype Server [ctx in out type address]
  Socket
  (configure [this]
    (map check-arg [ctx in out type address])
    (let [sock (mq/socket ctx (case type
                                :rep mq/rep
                                :xrep mq/xrep
                                :pair mq/pair
                                :sub mq/sub))]
      (debug "\nServer ready at: " address)
      (doto sock
        (.setSendTimeOut 60000)
        (.setReceiveTimeOut 60000)
        (.bind address))
      (go
       (loop []
         (try
          (>! out (readMsg (mq/recv-str sock)))
          (catch Exception e
;            (debug "<-....<-")
;            (debug "<-........<-")
;            (debug "<-............<-")
;            (debug "<-................<-")
;            (error "Can't retrieve message " e)
            ;(.close sock)
            ;(configure this)
            ))
         (recur)))
      (go
       (loop [msg (<! in)]
         (try
           (debug msg)
           (mq/send sock (writeMsg msg))
           (catch Exception e
;             (debug "->....->")
;             (debug "->........->")
;             (debug "->............->")
;             (debug "->................->")
             (error "Can't send message " e)
             ))
        (recur (<! in))))
      ))
  (->in [this msg]
    (go
     ;(debug "->in" msg)
     (>! in msg)))
  (<-out [this]
    ;(debug "<-out")
    (<!! out))
  (close [this]
    (close! in)
    (close! out)))
