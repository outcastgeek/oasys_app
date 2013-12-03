(ns oasysusa.form
  (:require
    [oasysusa.utils.form :as validation]))

(defn ^:export validate-form [initial-state-data]
  (let [initial-state-map (js->clj initial-state-data)
        form-state (atom {})]
    (reset! form-state initial-state-map)
    (validation/run form-state)))
