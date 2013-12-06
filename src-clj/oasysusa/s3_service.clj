(ns oasysusa.s3-service
  (:use clojure.tools.logging
        utils.zmq-api)
  (:require [clojure.core.reducers :as r]))

(def api-namespace
  (str *ns*))

(defn ^{:api api-namespace
        :doc "Prepends Hello to the input name"}
  world [name]
  (debug "Greeting ...")
  (format "Hello %s" name))

(defmulti addrange class)

(defmethod ^{:api api-namespace
        :doc "Adds elements of the list starting a 1 and ending at the provided lastItem"}
  addrange Integer [upperBound]
  (debug "Adding all ints...")
  (let [lst (range 1 upperBound)]
    (r/fold + lst)))

(defmethod ^{:api api-namespace
             :doc "Adds elements of the list starting a 1 and ending at the provided lastItem"}
  addrange Long [upperBound]
  (debug "Adding all longs...")
  (let [lst (range 1 upperBound)]
    (r/fold + lst)))

(defmethod ^{:api api-namespace
             :doc "Adds elements of the list starting a 1 and ending at the provided lastItem"}
  addrange Double [upperBound]
  (debug "Adding all doubles...")
  (let [lst (range 1 upperBound)]
    (r/fold + lst)))

(defmethod ^{:api api-namespace
        :doc "Adds elements of the list starting a 1 and ending at the provided lastItem"}
  addrange String [upperBound]
  (debug "Adding all strings...")
  (let [lst (range 1 (Integer/parseInt upperBound))]
    (r/fold + lst)))
