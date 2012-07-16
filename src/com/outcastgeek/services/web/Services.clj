(ns com.outcastgeek.services.web.Services
  (:use clojure.tools.logging
        clojure.pprint
        compojure.core
        compojure.handler
        ring.util.servlet
        ring.adapter.jetty
        ring.middleware.reload-modified
        com.outcastgeek.web.server.adapter.netty
        hiccup.core
        hiccup.page
        rrss
        somnium.congomongo
        com.outcastgeek.services.web.fluid
        com.outcastgeek.services.web.resumebuilder
        com.outcastgeek.config.AuthAuth
        com.outcastgeek.config.AppConfig
        com.outcastgeek.services.work.FuncDocCreator)
  (:import java.util.UUID
           java.util.concurrent.TimeUnit
           com.google.gson.Gson
           com.google.gson.GsonBuilder)
  (:require [ring.middleware.session :as rs]
            [hozumi.mongodb-session :as mongoss]
            [compojure.route :as route])
  (:gen-class :extends javax.servlet.http.HttpServlet))

(set-connection! mongo-connection)

(def mongoSessionStore
  (mongoss/mongodb-store {:auto-key-change? true
                          :collection-name :outcastgeek_sessions}))

(def one-minute 60)
(def fifteen-minutes (* 15 one-minute))
(def redisSessionStore
  (expiring-redis-store {:auto-key-change? true
                         :collection-name :outcastgeek_sessions
                         :duration fifteen-minutes
                         :resolution one-minute}))

(def glua gen-login-unless-auth)

(def gson
  (.. (GsonBuilder.) excludeFieldsWithoutExposeAnnotation create))

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
  (insert! :robots {:name "robby"})
  (page
    request
    (html-doc
      request
      "Services | "
      (html
        [:div {:class "row"}
         [:div {:class "span8"}
          [:h2 "Welcome"]
          [:div "Retrieved robot: " ((fetch-one :robots ) :name )]
          ]]
        [:div {:class "row"}
         [:div {:class "span8"}
          [:fieldset [:legend "{{SearchBy}}"]
           [:div {:class "clearfix"}
            [:div {:class "input"}
             [:input {:id "search" :name "search"
                      :required "required" :size "120"
                      :type "text" :ng-model "Criteria" :value "{{Criteria}}"}]]]
           [:button {:class "btn btn-primary" :ng-click "changeLocation()"} "Search"]]]
         "<div class='span8'  ng-view></div>"]))
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
                                                          #(% :name )) params session))

  ;Handle Google+ OAuth2 Callback
  (GET "/oauth2callback" {session :session params :params} ((login-processor
                                                              google-oauth2
                                                              goog-auth-req
                                                              "https://www.googleapis.com/oauth2/v1/userinfo"
                                                              "access_token"
                                                              #(% :name )) params session))

  ;Handle Dwolla OAuth2 Callback
  (GET "/oauth2cash" {session :session params :params} ((login-processor
                                                          dwolla-oauth2
                                                          dwolla-auth-req
                                                          "https://www.dwolla.com/oauth/rest/users"
                                                          "oauth_token"
                                                          #(-> % :Response :Name )) params session))

  ;Handle Paypal OAuth2 Callback
  (GET "/paypalCallback" {session :session params :params} ((login-processor
                                                          paypal-oauth2
                                                          paypal-auth-req
                                                          "https://identity.x.com/xidentity/resources/profile/me"
                                                          "oauth_token"
                                                          #(-> % :identity :fullName )) params session))

  ;Handle Flattr OAuth2 Callback
  (GET "/flattrCallback" {session :session params :params} ((login-processor
                                                              flattr-oauth2
                                                              flattr-auth-req
                                                              "https://api.flattr.com/rest/v2/user.json"
                                                              "oauth_token"
                                                              #(-> % :username )) params session))

  ;Handle Instagram OAuth2 Callback
  (GET "/instagramCallback" {session :session params :params} ((login-processor
                                                                 instagram-oauth2
                                                                 instagram-auth-req
                                                                 "https://api.instagram.com/v1/users/self"
                                                                 "access_token"
                                                                 #(-> % :data :username )) params session))

  ;Handle Live OAuth2 Callback
  (GET "/liveCallback" {session :session params :params} ((login-processor
                                                            live-oauth2
                                                            live-auth-req
                                                            "https://apis.live.net/v5.0/me"
                                                            "access_token"
                                                            #(% :username )) params session))

  ;Handle Foursquare OAuth2 Callback
  (GET "/foursquareCallback" {session :session params :params} ((login-processor
                                                                  foursquare-oauth2
                                                                  foursquare-auth-req
                                                                  "https://api.foursquare.com/v2/users/self"
                                                                  "oauth_token"
                                                                  #(-> % :response :user :firstName )) params session))

  ;Handle Github OAuth2 Callback
  (GET "/gitCallback" {session :session params :params} ((login-processor
                                                           git-oauth2
                                                           git-auth-req
                                                           "https://api.github.com/user"
                                                           "access_token"
                                                           #(% :login )) params session))

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
               (wrap-reload-modified ["src"])
               (wrap-session-expiry 900) ;; 15 mn
               (rs/wrap-session {:cookie-name "ogeeky-sessions"
                                 :store mongoSessionStore})
               ;                   (rs/wrap-session {:cookie-name "ogeeky-sessions"
               ;                                     :store redisSessionStore})
               (site)))

(defservice website)

(defn runJetty [portNumber]
  (info "Starting Jetty...")
  (run-jetty website {:port (Integer/parseInt portNumber)}))

(defn runNetty [portNumber]
  (info "Starting Netty...")
  (run-netty website {:port (Integer/parseInt portNumber)}))

(defn -main [server portNumber]
  (cond
    (= server "Jetty")
    (runJetty portNumber)
    (= server "Netty")
    (runNetty portNumber)
    )
  )
