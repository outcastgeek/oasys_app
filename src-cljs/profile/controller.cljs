(ns oasysusa.profile
  (:require [domina :as dom]
            [domina.events :as ev]
            [domina.css :as css]))

(def profile-state (atom {}))

(defn evt->el [event]
  (css/sel (aget (aget event "evt") "target")))

(defn update [event]
  (let [target-input (evt->el event)]
    (dom/log "KeyVal: " (dom/attr target-input "id") " => " (dom/value target-input))
    (swap! profile-state merge {(keyword (dom/attr target-input "id")) (dom/value target-input)})
    (dom/log @profile-state)
    ))

(defn reset []
  (swap! profile-state conj {}))

(defn is-unchanged [user]
  (if (not (nil? @profile-state))
    (= @profile-state (js->clj user))
    true))

(defn attach-blur-listener [target-id]
  (ev/listen! (dom/by-id target-id) :blur update))

(defn attach-events []
  (doall
   (map attach-blur-listener
       ["first_name" "last_name" "email" "date-of-birth" "address" "telephone_number"])))

(defn ^:export controller [$scope $http]
  ;(aset $scope "master" (clj->js @profile-state))
  (aset $scope "user" (fn [] (clj->js @profile-state)))
  (aset $scope "update" update)
  (aset $scope "reset" reset)
  (aset $scope "isUnchanged" is-unchanged)
  (reset)
  (attach-events))

(aset controller "$inject" (array "$scope"))
