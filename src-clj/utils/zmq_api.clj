(ns utils.zmq-api
  (:use clojure.tools.logging
        [clojure.string :only [split]]
        utils.zmq)
  (:require [clojure.core.reducers :as r]))

(defn- lookup-fn [namespaced-fn]
  (let [[namespace fun] (split namespaced-fn #"/")]
    (ns-resolve (symbol namespace) (symbol fun))))

(defn grab-api-fn [fun api-namespace]
  (debug (format "fun = %s and namespace = %s" fun api-namespace))
  (let [func (ns-resolve (symbol api-namespace) (symbol fun))]
    (if (nil? func)       
      identity
      func)
    ))

(defn api-info [api-namespace]
  (let [funcs (keys (ns-publics (symbol api-namespace)))
        ns-funcs (into () (r/map (fn [func]
                                  (let [nspace (format
                                                "%s/%s"
                                                api-namespace (str func))]
                           (lookup-fn nspace))) funcs))]
    (remove nil?
            (into () (r/map (fn [func]
                             (let [func-metadata (meta func)]
                               (when (not-nil? (func-metadata :api))
                                 (select-keys func-metadata [:doc :name :arglists])))) ns-funcs)))))

(def api-service-description "description")
