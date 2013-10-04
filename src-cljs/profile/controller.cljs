(ns oasysusa.profile
  (:require [domina :as dom]
            [domina.events :as ev]
            [domina.css :as css]))

(def profile-state (atom {:ids ["first_name" "last_name" "email" "date_of_birth" "address" "telephone_number"]}))

(defn evt->el [event]
  (css/sel (aget (aget event "evt") "target")))

(defn update [event]
  (let [target-input (evt->el event)]
    (dom/log "Profile Update KeyVal: " (dom/attr target-input "id") " => " (dom/value target-input))
    (swap! profile-state update-in [:profile]
           merge {(keyword (dom/attr target-input "id")) (dom/value target-input)})
    ;(dom/log @profile-state)
    ))

(defn update-input-value [id val]
  (dom/log "View Update KeyVal: " id " => " val)
  (dom/set-value! (dom/by-id id) val))

(defn reset []
  (swap! profile-state conj {}))

(defn is-unchanged [user]
  (if (not (nil? @profile-state))
    (= @profile-state (js->clj user))
    true))

(defn attach-blur-listener [target-id]
  (ev/listen! (dom/by-id target-id) :blur update))

(defn attach-change-listener [target-id]
  (ev/listen! (dom/by-id target-id) :change update))

(defn attach-keydown-listener [target-id]
  (ev/listen! (dom/by-id target-id) :keypress update))

(defn bootstrap []
  (let [input_ids (get-in @profile-state [:ids])]
    (doall
     (map (fn [id]
            (let [val (dom/value (dom/by-id id))]
              (swap! profile-state update-in [:profile]
                     merge {(keyword id) val}))) input_ids))
    (dom/log "Initial Profile State: " @profile-state)
    (doall
     (map attach-keydown-listener input_ids))
    (doall
     (map attach-change-listener input_ids))
    (doall
     (map attach-blur-listener input_ids))
    ))

(defn ^:export controller [$scope $http]
  ;(aset $scope "master" (clj->js @profile-state))
  ;(aset $scope "user" (fn [] (clj->js @profile-state)))
  (add-watch profile-state :profile
             (fn [_ _ old new]
               (let [new-profile (get-in @profile-state [:profile])]
                 (aset $scope "user" new-profile)
                 (dorun
                  (for [[k v] new-profile]
                    (update-input-value (name k) v)))
                 )))
  (aset $scope "update" update)
  (aset $scope "reset" reset)
  (aset $scope "isUnchanged" is-unchanged)
  (reset)
  (bootstrap))

(aset controller "$inject" (array "$scope"))
