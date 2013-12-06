(ns oasysusa.Client
  (:use clojure.tools.logging
        utils.zmq
        utils.zmq-api)
  (:require [clojure.core.async :as async :refer :all]
            [utils.zhelpers :as mq]))

(defn runClient [params]
  (let [{:keys [runs baseAddress func args]} params
        endpoints (into-array (map (fn [j] (str baseAddress j)) (range numberOfCpus)))
        num (Integer/parseInt runs)
        ctx (mq/context numberOfCpus)
        inputChannel (chan (sliding-buffer 1024))
        outputChannel (chan (sliding-buffer 1024))
        client (utils.zmq.Client. ctx inputChannel outputChannel :req endpoints)]
    (debug endpoints)
    (go
     (-> client configure))

    (->in client {:func api-service-description})
    (let [service-description (<-out client)]
      (debug "\nRemote Service Info: ")
      (spy (-> service-description :data))
      (debug service-description))
    
    (dotimes [i num]
      (->in client {:index (+ 1 i)
                    :total num
                    :func func
                    :data args})

      (let [contents (<-out client)
            {:keys [func data status]} contents]
        (debug "\nResult from " status ":\n [" args " => RemoteApi><" func "\n => " data "]"))
    )))

(defn -main [numberOfRuns func args]
  ;; Configure Threadpool
  (set-agent-send-off-executor! REQREP-EXECUTOR)
  
  ;; Start Client
  (runClient {:runs numberOfRuns
              :baseAddress "tcp://127.0.0.1:4444"
              :func func
              :args args}))
