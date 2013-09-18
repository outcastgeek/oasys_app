(ns oasysusa.profile
  (:require [clojure.browser.net :as net]))

(def profile-state (atom {}))

(def xhr (net/xhr-connection))

(defn ajax-json [url verb callback]
  (.send xhr url verb callback))

(defn update [user]
  (swap! profile-state user))

(defn reset []
  (swap! profile-state {}))

(defn is-unchanged [user]
  (= @profile-state user))

(defn get-profile [reply]
  (let [v (js->clj (.getResponseJson (.-target reply)))] ;v is a Clojure data structure
    (swap! profile-state v)))

(ajax-json "/employee" "GET" "" get-profile)

(defn ^:export controller [$scope $http]
  (aset $scope "update" update)
  (aset $scope "reset " reset)
  (aset $scope "isUnchanged" is-unchanged)
  (reset))

(aset controller "$inject" (array "$scope"))