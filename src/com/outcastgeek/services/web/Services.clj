(ns com.outcastgeek.services.web.Services
  (:use clojure.tools.logging
        clojure.pprint
        compojure.core
        [ring.util response servlet]
        ring.adapter.netty
        hiccup.core
        hiccup.page
        clj-time.coerce
        com.outcastgeek.services.web.fluid
        com.outcastgeek.util.MiddleWares
        com.outcastgeek.config.AuthAuth
        com.outcastgeek.config.AppConfig
        com.outcastgeek.domain.Entities
        com.outcastgeek.services.web.Payroll
        ;        com.outcastgeek.services.work.FuncDocCreator
        com.outcastgeek.util.Jobs)
  (:import java.util.UUID
           org.quartz.ObjectAlreadyExistsException
           com.outcastgeek.web.server.runner.Jetty)
  (:require [compojure.route :as route]
            [clj-time.core :as t]
            [cheshire.core :refer [generate-string]])
  (:gen-class :extends javax.servlet.http.HttpServlet))

(def glua gen-login-unless-auth)

(defn page
  [request html sessMerge]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body html
   :session (merge (request :session ) sessMerge)})

(defn home [request]
  ;(enQueueStuff "Homey!!!!")
  (page
    request
    (html-doc
      request
      "Services | "
      (html
        "<div class='hero-unit'  ng-view></div>"
        [:div {:class "row" :ng-include "'/views/carousel.html'"}]
        [:div {:class "row" :ng-include "'/views/facts.html'"}]
        ))
    {:flash "" :flashstyle ""}))

(defn about [request]
  (let [csrf (str (UUID/randomUUID))
        session (request :session )]
    (page
      request
      (html-doc
        request
        "About | "
        (html
          [:div {:class "hero-unit"}
           [:h1 "What is this Web App?"]
           [:p "It is an awesome piece of teachnology! It's magical."]]
          ))
      {:csrf csrf :flash "" :flashstyle ""}
      )))

(defn login [request]
  (let [csrf (str (UUID/randomUUID))
        session (request :session )]
    (page
      request
      (html-doc
        request
        "Log in | "
        (html
          [:div {:class "row"}
           [:div {:class "span16"}
            [:h2 "Log in"]
            [:br ]
            [:form {:method "post" :action "/login" :enctype "application/x-www-form-urlencoded"}
             [:fieldset [:legend "Enter your username and password"]
              (input "User ID" "uniq" session)
              (secret-input "Password" "password" session)
              [:input {:type "hidden" :name "csrf" :value csrf}]
              [:input {:class "btn btn-primary" :type "submit" :value "Login"}]
              ;              [:span " or "] (button-link-to "/register" "Register ")
              ]]]]
          ))
      {:csrf csrf :flash "" :flashstyle ""})))

(defn register [request]
  (let [csrf (str (UUID/randomUUID))
        session (request :session )]
    (page
      request
      (html-doc
        request
        "Register | "
        (html
          [:div {:class "row"}
           [:div {:class "span12"}
            [:h2 "Register"]
            [:br ]
            [:form {:method "post" :action "/register" :enctype "application/x-www-form-urlencoded"}
             [:fieldset [:legend "Enter your information"]
              (input "User Name" "username" session)
              (input "Email" "email" session)
              (secret-input "Password" "password" session)
              (secret-input "Confirm Password" "confirmpassword" session)
              [:input {:type "hidden" :name "csrf" :value csrf}]
              [:input {:class "btn btn-primary" :type "submit" :value "Register"}]
              ]]]]
          ))
      {:csrf csrf :flash "" :flashstyle ""})))

(defn profileData [request]
  (let [session (request :session )
        csrf (str (UUID/randomUUID))
        username (session :username )
        uniq (-> session :user-info :uniq )
        employee (first (findExistingEmployee {:uniq uniq
                                               :username username}))
        ;employeeData (select-keys employee [:first_name :last_name :username :email :active])
        employeeData (merge (select-keys employee
                              [:first_name :last_name :username :email :active]) {:csrf csrf})]
    (debug "FOUND EMPLOYEE: " employee)
    (do {:status 200
         :headers {"Content-Type" "application/json"}
         :body (generate-string employeeData)
         :session (merge (request :session ) {:csrf csrf})
         })))

(defn profile [request]
  (let [session (request :session)
        csrf (str (UUID/randomUUID))
        username (session :username)
        uniq (-> session :user-info :uniq)
        employee (first (findExistingEmployee {:uniq uniq
                                               :username username}))]
    (debug "FOUND EMPLOYEE: " employee)
    (page
      request
      (html-doc
        request
        "Profile | "
        (html
          [:div {:class "hero-unit"}
           [:h1 username]
           [:p "profile details:"]]
          [:div {:ng-controller "ProfileCtrl" :class "row"}
           [:div {:class "span6"}
            [:h2 "Info"]
            [:ul [:li "User Name: " "{{profile.username}}"]
             [:li "First Name: " "{{profile.first_name}}"]
             [:li "Last Name: " "{{profile.last_name}}"]
             [:li "Email: " "{{profile.email}}"]
             [:li "Active: " "{{profile.active}}"]]]
           [:div {:class "span6"}
            [:h2 "Update Your Profile"]
            [:br ]
            ;[:form {:method "post" :action "/profile" :enctype "application/x-www-form-urlencoded"} ;TODO: Use that at some point: :ng-submit "updateProfile()"
            [:form {:ng-submit "updateProfile()"}
             [:fieldset [:legend "Enter values only for the information you would like to update"]
              (input "First Name" "first_name" {:model "profile.first_name" :placeholder "first name" :value "{{profile.first_name}}"})
              (input "Last Name" "last_name" {:model "profile.last_name" :placeholder "last name" :value "{{profile.last_name}}"})
              (input "User Name" "username" {:model "profile.username" :placeholder "user name" :value "{{profile.username}}"})
              (input "Email" "email" {:model "profile.email" :placeholder "email@email.com" :value "{{profile.email}}"})
              (secret-input "Password" "password" session :required false)
              (secret-input "Confirm Password" "confirmpassword" session :required false)
              [:input {:type "hidden" :name "csrf" :value csrf}]
              [:input {:class "btn btn-primary" :type "submit" :value "Update"}]
              ]]]
           ])
        :pagejs "/static/javascript/oasys_profile.js")
      {:csrf csrf :flash "" :flashstyle ""})))

(defn timesheets [request]
  (let [csrf (str (UUID/randomUUID))
        rightNow (t/now)
        session (request :session )
        username (session :username )
        uniq (-> session :user-info :uniq )
        employee (first (findExistingEmployee {:uniq uniq
                                               :username username}))
        currentTimeSheet (first (findEmployeeCurrentTimesheet {:employee_id (employee :id )
                                                               :start_date (to-sql-date (firstDayOfTheWeekOf rightNow))
                                                               :end_date (to-sql-date (lastDayOfTheWeekOf rightNow))}))
        workSegments (findWorksegment {:employee_id (employee :id )
                                       :timesheet_id (currentTimeSheet :id )})]
    (debug (employee :username ) "'s current timesheet's work segments: " workSegments)
    (page
      request
      (html-doc
        request
        "Profile | "
        (html
          [:div {:class "hero-unit"}
           [:h1 username]
           [:p "Timesheets:"]]
          [:div {:class "row"}
           [:div {:class "span6"}
            [:h2 "Info"]
            [:ul [:li "First Name: " (employee :first_name )]
             [:li "Last Name: " (employee :last_name )]
             [:li "Email: " (employee :email )]
             [:li "Active: " (employee :active )]]]
           [:div {:class "span6"}
            [:h2 "Update Your Profile"]
            [:br ]
            [:form {:method "post" :action "/profile" :enctype "application/x-www-form-urlencoded"}
             [:fieldset [:legend "Enter values only for the information you would like to update"]
              (input "First Name" "first_name" (session :user-info ))
              (input "Last Name" "last_name" (session :user-info ))
              (input "User Name" "username" session)
              (input "Email" "email" (session :user-info ))
              (secret-input "Password" "password" session :required false)
              (secret-input "Confirm Password" "confirmpassword" session :required false)
              [:input {:type "hidden" :name "csrf" :value csrf}]
              [:input {:class "btn btn-primary" :type "submit" :value "Update"}]
              ]]]
           ]))
      {:csrf csrf :flash "" :flashstyle ""})))

(defn oauth-redirect [request]
  (let [provider (-> request :params :provider keyword)
        session (request :session )]
    (debug (-> oauth-providers provider :uri ))
    (do
      {:status 302
       :headers {"Location" (-> oauth-providers provider :uri )}
       :session (merge session {:provider provider})
       })))

(defroutes main-routes

  (GET "/login" request (login request))

  (POST "/login" {session :session params :params} (login-controller params session))

  (ANY "/logout" request ((glua (auth-req? request) logout-controller) request))

  (GET "/profile" request ((glua (auth-req? request) profile) request))

  (GET "/profileData" request ((glua (auth-req? request) profileData) request))

  (POST "/profile" {session :session params :params} (profile-controller params session))

  (GET "/timesheets" request ((glua (auth-req? request) timesheets) request))

  (POST "/timesheets" {session :session params :params} (timesheets-controller params session))

  ;(ANY "/*" (ensure-admin-controller session))
  ;(ANY "/admin/" (admin-view session))

  (GET "/" request (home request))

  ;  (GET "/register" request (register request))
  ;
  ;  (POST "/register" {session :session params :params} (register-controller params session))

  (GET "/about" request (about request))

  ;Redirect to OAuth2 Provider
  (GET "/login/:provider" request (oauth-redirect request))

  ;Handle Facebook OAuth2 Callback
  (GET "/auth/login" {session :session params :params} ((login-processor
                                                          facebook-oauth2
                                                          fb-auth-req
                                                          "https://graph.facebook.com/me"
                                                          "access_token"
                                                          (fn [user-data]
                                                            {:username (user-data :name )
                                                             :first_name (user-data :first_name )
                                                             :last_name (user-data :last_name )
                                                             :email (str (user-data :username ) "@facebook.com")
                                                             :uniq (user-data :id )
                                                             :provider "fb"})) params session))

  ;Handle Google+ OAuth2 Callback
  (GET "/oauth2callback" {session :session params :params} ((login-processor
                                                              google-oauth2
                                                              goog-auth-req
                                                              "https://www.googleapis.com/oauth2/v1/userinfo"
                                                              "access_token"
                                                              (fn [user-data]
                                                                {:username (user-data :name )
                                                                 :first_name (user-data :given_name )
                                                                 :last_name (user-data :family_name )
                                                                 :email (user-data :email )
                                                                 :uniq (user-data :id )
                                                                 :provider "goog"})) params session))

  ;Handle Dwolla OAuth2 Callback
  (GET "/oauth2cash" {session :session params :params} ((login-processor
                                                          dwolla-oauth2
                                                          dwolla-auth-req
                                                          "https://www.dwolla.com/oauth/rest/users"
                                                          "oauth_token"
                                                          (fn [user-data]
                                                            {:username (-> user-data :Response :Name )
                                                             :first_name (-> user-data :Response :Name )
                                                             :last_name (-> user-data :Response :Name )
                                                             :uniq (-> user-data :Response :Id )
                                                             :provider "dwolla"})) params session))

  ;Handle Paypal OAuth2 Callback
  (GET "/paypalCallback" {session :session params :params} ((login-processor
                                                              paypal-oauth2
                                                              paypal-auth-req
                                                              "https://identity.x.com/xidentity/resources/profile/me"
                                                              "oauth_token"
                                                              (fn [user-data]
                                                                {:username (-> user-data :identity :fullName )
                                                                 :first_name (-> user-data :identity :firstName )
                                                                 :last_name (-> user-data :identity :lastName )
                                                                 :email (-> user-data :identity :emails :first )
                                                                 :uniq (-> user-data :identity :userId )
                                                                 :payerId (-> user-data :identity :payerID )
                                                                 :provider "paypal"})) params session))

  ;Handle Flattr OAuth2 Callback
  (GET "/flattrCallback" {session :session params :params} ((login-processor
                                                              flattr-oauth2
                                                              flattr-auth-req
                                                              "https://api.flattr.com/rest/v2/user"
                                                              "access_token"
                                                              (fn [user-data]
                                                                {:username (-> user-data :username )
                                                                 :first_name (-> user-data :firstname )
                                                                 :last_name (-> user-data :lastname )
                                                                 :uniq (-> user-data :link )
                                                                 :email (-> user-data :email )
                                                                 :provider "flattr"})) params session))

  ;Handle Instagram OAuth2 Callback
  (GET "/instagramCallback" {session :session params :params} ((login-processor
                                                                 instagram-oauth2
                                                                 instagram-auth-req
                                                                 "https://api.instagram.com/v1/users/self"
                                                                 "access_token"
                                                                 (fn [user-data]
                                                                   {:username (-> user-data :data :username )
                                                                    :first_name (-> user-data :data :full_name )
                                                                    :last_name (-> user-data :data :full_name )
                                                                    :uniq (-> user-data :data :id )
                                                                    :provider "instagram"})) params session))

  ;Handle Live OAuth2 Callback
  (GET "/liveCallback" {session :session params :params} ((login-processor
                                                            live-oauth2
                                                            live-auth-req
                                                            "https://apis.live.net/v5.0/me"
                                                            "access_token"
                                                            (fn [user-data]
                                                              {:username (user-data :username )
                                                               :provider "live"})) params session))

  ;Handle Foursquare OAuth2 Callback
  (GET "/foursquareCallback" {session :session params :params} ((login-processor
                                                                  foursquare-oauth2
                                                                  foursquare-auth-req
                                                                  "https://api.foursquare.com/v2/users/self"
                                                                  "oauth_token"
                                                                  (fn [user-data]
                                                                    {:username (-> user-data :response :user :firstName )
                                                                     :first_name (-> user-data :response :user :firstName )
                                                                     :last_name (-> user-data :response :user :lastName )
                                                                     :email (-> user-data :response :user :contact :email )
                                                                     :uniq (-> user-data :response :user :id )
                                                                     :provider "foursquare"})) params session))

  ;Handle Github OAuth2 Callback
  (GET "/gitCallback" {session :session params :params} ((login-processor
                                                           git-oauth2
                                                           git-auth-req
                                                           "https://api.github.com/user"
                                                           "access_token"
                                                           (fn [user-data]
                                                             {:username (user-data :login )
                                                              :uniq (user-data :id )
                                                              :provider "git"})) params session))

  (route/resources "/")

  (ANY "*" request
    (pprint request)
    (html-doc
      request
      "Page Not Found | "
      [:div {:class "row"}
       [:div {:class "span16"}
        [:h1 "Aw snap! You broke the internet."]
        [:small "Page Not Found"]
        ]]))
  )

(def website (-> main-routes
               (wrap-og-filters)))

(defservice website)

(defn runJetty [portNumber webXml]
  (info "Starting Jetty...")
  (Jetty/runServer (Integer/parseInt portNumber) webXml))

(defn runNetty [portNumber]
  (info "Starting Netty...")
  (run-netty website {:port (Integer/parseInt portNumber)
                      :netty {"reuseAddress" true}}))

(defn -main [server portNumber]
  ;(defn -main [server portNumber webXml]
  ;; Starting Scheduled Jobs
  (try
    (runJobs)
    (catch ObjectAlreadyExistsException e
      (error "Exception message: " (.getMessage e)))
    (finally
      (info "Proceeding ....")))
  (cond
    (= server "Jetty")
    (runJetty portNumber "web.xml")
    ;(runJetty portNumber webXml)
    (= server "Netty")
    (runNetty portNumber)
    ))
