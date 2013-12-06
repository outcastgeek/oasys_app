(ns oasysusa.core
  (:use clojure.tools.logging
        utils.zmq
        utils.zmq-api
        oasysusa.s3-service)
  (:require [clojure.core.async :as async :refer :all]
            [utils.zhelpers :as mq]))

(defn describeService [server name endpoint]
  (let [description {:func api-service-description
                     :data (api-info api-namespace)
                     :status (format "Result from %s @ %s"
                               name endpoint)}]
    (debug "\nService Description: ")
    ;(spy [:name :arglists :doc] (-> description :data))
    (spy (-> description :data))
    (->in server description)))

(defn executeServiceMethod [server name endpoint]
  (let [contents (<-out server)
        {:keys [func data index total]} contents
        funct (grab-api-fn func api-namespace)]
    (if (= api-service-description func)
      (describeService server name endpoint)
      (do
        (debug "\n" endpoint " received message #" index
          " out of " total " with contents:\n" contents)
        (debug "Invoking " func " on " name "\n with arguments " data)
        (->in server {:func func
                      :data (funct data)
                      :status (format "%s @ %s"
                                name endpoint)})
        )))
  (recur server name endpoint))

(defn runWorker [number baseAdress]
  (debug "Setting up worker #" number)
  (let [name (format "worker%d" number)
        endpoint (format "%s%d"  baseAdress number)
        ctx (mq/context numberOfCpus)
        inputChannel (chan (sliding-buffer 1024))
        outputChannel (chan (sliding-buffer 1024))
        server (utils.zmq.Server. ctx inputChannel outputChannel :rep endpoint)]
    (go
      (-> server configure))

    (executeServiceMethod server name endpoint)
    ))

(defn runServer [baseAddress]
  (pmap (fn [k] (runWorker k baseAddress)) (range numberOfCpus)))

(defn -main []
  ;; Configure Threadpool
  (set-agent-send-off-executor! REQREP-EXECUTOR)

  ;; Start Server
  (runServer "tcp://127.0.0.1:4444"))

;;;;;;;;;;;;;;;;;;;;;;;;;

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

