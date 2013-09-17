(ns phone-cat.app
  (:require [phone-cat.controllers :as ctrl]))

(defn ng-route [provider path route-spec]
  (.when provider path (clj->js route-spec)))

(defn ng-route-otherwise [provider route-spec]
  (.otherwise provider (clj->js route-spec)))

(defn start-symbol [provider start]
  (.startSymbol provider start))

(defn end-symbol [provider end]
  (.endSymbol provider end))

(doto (angular/module "phonecat" (array "phonecatFilters" "phonecatServices"))
  (.config (array "$routeProvider" "$interpolateProvider"
                  (fn [$routeProvider $interpolateProvider]
                    (doto $interpolateProvider
                      (start-symbol "{@")
                      (end-symbol "@}"))
                    (doto $routeProvider
                      (ng-route "/phones" {:templateUrl "/partials/phone-list";"partials/phone-list.html"
                                           :controller ctrl/phone-list-ctrl})
                      (ng-route "/phones/:phoneId" {:templateUrl "/partials/phone-detail";"partials/phone-detail.html"
                                                    :controller ctrl/phone-detail-ctrl})
                      (ng-route-otherwise {:redirectTo "/phones"})))
             )))
