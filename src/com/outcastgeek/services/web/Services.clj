(ns com.outcastgeek.services.web.Services
  (:use clojure.tools.logging
        clojure.pprint
        compojure.core
        compojure.handler
        ring.util.servlet
;        ring.adapter.jetty
        ring.adapter.netty
        hiccup.core
        hiccup.page
        somnium.congomongo
        com.outcastgeek.services.web.fluid
        com.outcastgeek.services.web.resumebuilder
        com.outcastgeek.config.AuthAuth
        com.outcastgeek.config.AppConfig
        com.outcastgeek.domain.Entities
        com.outcastgeek.services.work.FuncDocCreator)
  (:import java.util.UUID
           com.outcastgeek.web.server.runner.Jetty)
  (:require [ring.middleware.session :as rs]
            [hozumi.mongodb-session :as mongoss]
            [compojure.route :as route]
            [resque-clojure.core :as resque])
  (:gen-class :extends javax.servlet.http.HttpServlet))

(set-connection! mongo-connection)

(def mongoSessionStore
  (mongoss/mongodb-store {:auto-key-change? true
                          :collection-name :outcastgeek_sessions}))

(def glua gen-login-unless-auth)

(defn page
  [request html sessMerge]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body html
   :session (merge (request :session ) sessMerge)})

;(defn admin-view [session]
;  (page session "Admin"
;    [:p "Only admin can see this page."]))

(defn resume [request]
  (let [csrf (str (UUID/randomUUID))
        step ((request :params ) :step )
        session (request :session )]
    (page
      request
      (html-doc
        request
        "Build Your Resume Like A Boss | Free, Easy & Awesome | "
        (cond
          (nil? step)
          (resume-get-step 0 csrf session)
          :else (resume-get-step step csrf session)))
      {:csrf csrf :flash "" :flashstyle ""})))

(defn downloadResume [request]
  (let [session (request :session )]
    (debug "Downloading resume as PDF document...")
    (do
      {:status 200
       :headers {"Content-Type" "application/pdf"}
       :body (generatePdfFromHtml (fullResume session))})
    ))

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
              (input "User Name" "username" session)
              (secret-input "Password" "password" session)
              [:input {:type "hidden" :name "csrf" :value csrf}]
              [:input {:class "btn btn-primary" :type "submit" :value "Login"}]
              [:span " or "] (button-link-to "/register" "Register ")
              ]]]]
          (goog-analytics)
          ))
      {:csrf csrf :flash "" :flashstyle ""})))

;(defn home [request]
;  (insert! :robots
;           {:name "robby"})
;  (page
;   request
;   (html-doc
;    request
;    "Services"
;    (html
;     [:div {:class "row"}
;      [:div {:class "span16"}
;       [:h2 "Welcome"]
;       [:div "Retrieved robot: " ((fetch-one :robots) :name)]
;       ]]
;     (include-js "/static/js/out/hello/hello.js")
;     (javascript-tag
;      "
;      alert(greet('ClojureScript'));
;      alert('The sum of [1,2,3,4,5,6,7,8,9] is: ' + hello.core.sum([1,2,3,4,5,6,7,8,9]));
;      ")
;     ))
;   {:flash "" :flashstyle ""}))

(defn home-other [request]
  (do
    {:status 302
     :headers {"Location" "http://signup.upgradeavenue.com"}
     }))

;(defn async-home [channel request]
;  (enqueue channel
;    (dosync
;      (insert! :robots
;        {:name "robby"})
;      (page
;        request
;        (html-doc
;          request
;          "Services | "
;          (html
;            [:div {:class "row"}
;             [:div {:class "span16"}
;              [:h2 "Welcome"]
;              [:div "Retrieved robot: " ((fetch-one :robots) :name)]
;              ]]))
;        {:flash "" :flashstyle ""}))))

(defn home [request]
;  (insert! :robots {:name "robby"})
;  (debug "EMPLOYEES: " (allEmployees))
  (page
    request
    (html-doc
      request
      "Services | "
      (html
;        [:div {:class "row"}
;         [:div {:class "span8"}
;          [:h2 "Welcome"]
;          [:div "Retrieved robot: " ((fetch-one :robots ) :name )]
;          ]]
        "<div class='hero-unit'  ng-view></div>"
        [:div {:class "row" :ng-include "'/views/carousel.html'"}]
        [:div {:class "row" :ng-include "'/views/facts.html'"}]
        ))
    {:flash "" :flashstyle ""}))

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
           [:div {:class "span16"}
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
          (goog-analytics)
          ))
      {:csrf csrf :flash "" :flashstyle ""})))

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

(defn bogus [request]
  (let [csrf (str (UUID/randomUUID))
        session (request :session )]
    (page
      request
      (html-doc
        request
        "Bogus | A page full of bogus"
        (html
          [:div {:class "row"}
           [:div {:class "span14 hero-unit"}
            [:h1 "What is this BOGUS page?"]
            [:p "It's a route to try out bogus stuff!"]
            ]]
          [:table {:align "center"}
           [:tr [:td {:colspan "2" :style "font-weight:bold;"} "Please enter your name:"]]
           [:tr [:td {:id "nameFieldContainer"}]
            [:td {:id "sendButtonContainer"}]]
           [:tr [:td {:colspan "2" :style "color:red;" :id "errorLabelContainer"}]]]
          (include-js "/static/js/mywebapp/mywebapp.nocache.js")
          ))
      {:csrf csrf :flash "" :flashstyle ""}
      )))

(defn profile [request]
  (let [session (request :session)
        username (session :username)
        uniq (-> session :user-info :uniq)
        employee (first (findEmployee {:uniq uniq}))]
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
        [:div {:class "row"}
         [:div {:class "span6"}
          [:h2 "Info"]
          [:ul
           [:li "First Name" (employee :first_name)]
           [:li "Last Name: " (employee :last_name)]
           [:li "Email: " (employee :email)]
           [:li "Active: " (employee :active)]]]
         ]))
    {:flash "" :flashstyle ""})))

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

  (ANY "/logout" [] (logout-controller))
  
  (GET "/profile" request ((glua (auth-req? request) profile) request))

  ;(ANY "/*" (ensure-admin-controller session))
  ;(ANY "/admin/" (admin-view session))

  (GET "/resume" request (resume request))
  ;(GET "/resume/:step" request ((glua (auth-req? request) resume) request))
  (GET "/resume/:step" request (resume request))

  (GET "/resume/:step/add" request (resume request))

  (GET "/resume/:step/remove/:index" request (resume request))

  (POST "/resume" {session :session params :params} (resume-handler params session))

  (GET "/downloadResume" request (downloadResume request))

  (GET "/" request (home request))

  (GET "/register" request (register request))

  (POST "/register" {session :session params :params} (register-controller params session))

  (GET "/about" request (about request))

  (GET "/bogus" request (bogus request))

  ;Redirect to OAuth2 Provider
  (GET "/login/:provider" request (oauth-redirect request))

  ;Handle Facebook OAuth2 Callback
  (GET "/auth/login" {session :session params :params} ((login-processor
                                                          facebook-oauth2
                                                          fb-auth-req
                                                          "https://graph.facebook.com/me"
                                                          "access_token"
                                                          (fn [user-data]
                                                            {:username (user-data :name)
                                                             :first_name (user-data :first_name)
                                                             :last_name (user-data :last_name)
                                                             :email (str (user-data :username) "@facebook.com")
                                                             :uniq (user-data :id)
                                                             :provider "fb"})) params session))

  ;Handle Google+ OAuth2 Callback
  (GET "/oauth2callback" {session :session params :params} ((login-processor
                                                              google-oauth2
                                                              goog-auth-req
                                                              "https://www.googleapis.com/oauth2/v1/userinfo"
                                                              "access_token"
                                                              (fn [user-data]
                                                                {:username (user-data :name)
                                                                 :first_name (user-data :given_name)
                                                                 :last_name (user-data :family_name)
                                                                 :email (user-data :email)
                                                                 :uniq (user-data :id)
                                                                 :provider "goog"})) params session))

  ;Handle Dwolla OAuth2 Callback
  (GET "/oauth2cash" {session :session params :params} ((login-processor
                                                          dwolla-oauth2
                                                          dwolla-auth-req
                                                          "https://www.dwolla.com/oauth/rest/users"
                                                          "oauth_token"
                                                          (fn [user-data]
                                                            {:username (-> user-data :Response :Name)
                                                             :first_name (-> user-data :Response :Name)
                                                             :last_name (-> user-data :Response :Name)
                                                             :uniq (-> user-data :Response :Id)
                                                             :provider "dwolla"})) params session))

  ;Handle Paypal OAuth2 Callback
  (GET "/paypalCallback" {session :session params :params} ((login-processor
                                                          paypal-oauth2
                                                          paypal-auth-req
                                                          "https://identity.x.com/xidentity/resources/profile/me"
                                                          "oauth_token"
                                                          (fn [user-data]
                                                            {:username (-> user-data :identity :fullName)
                                                             :first_name (-> user-data :identity :firstName)
                                                             :last_name (-> user-data :identity :lastName)
                                                             :email (-> user-data :identity :emails :first)
                                                             :uniq (-> user-data :identity :userId)
                                                             :payerId (-> user-data :identity :payerID)
                                                             :provider "paypal"})) params session))

  ;Handle Flattr OAuth2 Callback
  (GET "/flattrCallback" {session :session params :params} ((login-processor
                                                              flattr-oauth2
                                                              flattr-auth-req
                                                              "https://api.flattr.com/rest/v2/user"
                                                              "access_token"
                                                              (fn [user-data]
                                                                {:username (-> user-data :username)
                                                                 :first_name (-> user-data :firstname)
                                                                 :last_name (-> user-data :lastname)
                                                                 :uniq (-> user-data :link)
                                                                 :email (-> user-data :email)
                                                                 :provider "flattr"})) params session))

  ;Handle Instagram OAuth2 Callback
  (GET "/instagramCallback" {session :session params :params} ((login-processor
                                                                 instagram-oauth2
                                                                 instagram-auth-req
                                                                 "https://api.instagram.com/v1/users/self"
                                                                 "access_token"
                                                                 (fn [user-data]
                                                                   {:username (-> user-data :data :username)
                                                                    :first_name (-> user-data :data :full_name)
                                                                    :last_name (-> user-data :data :full_name)
                                                                    :uniq (-> user-data :data :id)
                                                                    :provider "instagram"})) params session))

  ;Handle Live OAuth2 Callback
  (GET "/liveCallback" {session :session params :params} ((login-processor
                                                            live-oauth2
                                                            live-auth-req
                                                            "https://apis.live.net/v5.0/me"
                                                            "access_token"
                                                            (fn [user-data]
                                                              {:username (user-data :username)
                                                               :provider "live"})) params session))

  ;Handle Foursquare OAuth2 Callback
  (GET "/foursquareCallback" {session :session params :params} ((login-processor
                                                                  foursquare-oauth2
                                                                  foursquare-auth-req
                                                                  "https://api.foursquare.com/v2/users/self"
                                                                  "oauth_token"
                                                                  (fn [user-data]
                                                                    {:username (-> user-data :response :user :firstName)
                                                                     :first_name (-> user-data :response :user :firstName)
                                                                     :last_name (-> user-data :response :user :lastName)
                                                                     :email (-> user-data :response :user :contact :email)
                                                                     :uniq (-> user-data :response :user :id)
                                                                     :provider "foursquare"})) params session))

  ;Handle Github OAuth2 Callback
  (GET "/gitCallback" {session :session params :params} ((login-processor
                                                           git-oauth2
                                                           git-auth-req
                                                           "https://api.github.com/user"
                                                           "access_token"
                                                           (fn [user-data]
                                                             {:username (user-data :login)
                                                              :uniq (user-data :id)
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
               (wrap-session-expiry 900) ;; 15 mn
               (rs/wrap-session {:cookie-name "ogeeky-sessions"
                                 :store mongoSessionStore})
               (site)))

(defservice website)

;(defn runJetty [portNumber]
;  (info "Starting Jetty...")
;  (run-jetty website {:port (Integer/parseInt portNumber)}))

(defn runJetty [portNumber webXml]
  (info "Starting Jetty...")
  (Jetty/runServer (Integer/parseInt portNumber) webXml))

(defn runNetty [portNumber]
  (info "Starting Netty...")
  (run-netty website {:port (Integer/parseInt portNumber)
                  :netty {"reuseAddress" true}}))

(defn -main [server portNumber webXml]
  ;; listening for jobs
  (resque/start ["createNewEmployeeQueue"
                 "sendNewMailQueue"])
  (cond
    (= server "Jetty")
    (runJetty portNumber webXml)
    (= server "Netty")
    (runNetty portNumber)
    ))
