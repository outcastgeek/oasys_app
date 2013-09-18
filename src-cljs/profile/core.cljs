(ns oasysusa.profile.core
  (:require [oasysusa.profile :as prfl]))

(defn start-symbol [provider start]
  (.startSymbol provider start))

(defn end-symbol [provider end]
  (.endSymbol provider end))

(doto (angular/module "profile" (array))
  (.config (array "$interpolateProvider"
             (fn [$interpolateProvider]
               (doto $interpolateProvider
                 (start-symbol "{@")
                 (end-symbol "@}")))
             )))
