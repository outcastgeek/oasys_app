(ns oasysusa.profile.core
  (:require [clojure.string :as cljstr]
            [oasysusa.profile :as prfl]))

(defn start-symbol [provider start]
  (.startSymbol provider start))

(defn end-symbol [provider end]
  (.endSymbol provider end))

(defn format-date-on-model [ng-model raw-date-string]
  (let [formated-date (cljstr/join "/" (reverse (cljstr/split raw-date-string #"-")))]
    (doto ng-model
      (.$setViewValue formated-date)
      (.$render))))

(defn formInput [scope element attr ng-model]
;  (.log js/console scope element attr)
;  (.log js/console ng-model)
;  (.log js/console "attr = " attr)
  (if (not (nil? ng-model))
    (let [value (.val element)]
      (if (= "date" (aget attr "inputType"))
        (format-date-on-model ng-model value)
        (.$setViewValue ng-model value))
      )))

(doto (angular/module "profile" (array))
  (.config (array "$interpolateProvider"
             (fn [$interpolateProvider]
               (doto $interpolateProvider
                 (start-symbol "{@")
                 (end-symbol "@}")))
             ))
  (.directive "formInput" (fn []
                            (clj->js {:require "ngModel"
                                      :link formInput}))))
