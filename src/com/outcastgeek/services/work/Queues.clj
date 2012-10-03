(ns com.outcastgeek.services.work.Queues
  (:use aleph.http
        aleph.redis
        lamina.core
        com.outcastgeek.config.AppConfig))

(def consumer
  (redis-client {:host (appProperties :redis-url)
                :port (appProperties :redis-port)}))

(def producer
  (redis-client {:host (appProperties :redis-url)
                 :port (appProperties :redis-port)}))

(defn queueWork [queue data]
  (enqueue-task producer queue data))

(defn assignWorker [queue worker]
  ;(async (worker (receive-task consumer queue)))
  (worker (receive-task consumer queue))
  )
