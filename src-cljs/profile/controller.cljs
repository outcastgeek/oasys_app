(ns oasysusa.profile
;  (:require [clojure.browser.net :as net])
  )

(def profile-state (atom nil))

;(def xhr (net/xhr-connection))
;
;(defn ajax-json [url verb callback]
;  (.send xhr url verb callback))

(defn update [user]
  (swap! profile-state conj (js->clj user)))

(defn reset []
  (swap! profile-state conj {}))

(defn is-unchanged [user]
  (if (not (nil? @profile-state))
    (= @profile-state (js->clj user))
    true))

;(defn get-profile [reply]
;  (let [v (js->clj (.getResponseJson (.-target reply)))] ;v is a Clojure data structure
;    (.log js/console v)
;    (swap! profile-state v)))

;(ajax-json "/employee" "GET" get-profile)

(defn ^:export controller [$scope $http]
  ;(aset $scope "master" (clj->js @profile-state))
  (aset $scope "user" (fn [] (clj->js @profile-state)))
  (aset $scope "update" update)
  (aset $scope "reset" reset)
  (aset $scope "isUnchanged" is-unchanged)
  (reset))

(aset controller "$inject" (array "$scope"))
