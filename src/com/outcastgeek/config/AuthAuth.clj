(ns com.outcastgeek.config.AuthAuth
  (:use clojure.tools.logging
        somnium.congomongo
        com.outcastgeek.config.AppConfig
        com.outcastgeek.domain.Entities)
  (:require [ring.middleware.session :as rs]
            [clj-oauth2.client :as oauth2]
            [cheshire.core :refer [parse-string]])
  (:import java.util.UUID))

(set-connection! mongo-connection)

(def ^:dynamic *hash-delay* 1000)

(defn gen-salt
  "Generate a secure salt."
  ([] (gen-salt 8))
  ([n]
     (let [random (java.security.SecureRandom.)
           salt (make-array Byte/TYPE n)]
       (.nextBytes random salt)
       salt)))

(defn hash-password
  "Generate a hash from the password and salt. Default value of n is
   *hash-delay*."
  ([password salt] (hash-password password salt *hash-delay*))
  ([password salt n]
     (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
       (do (.reset digest)
           (.update digest (if (string? salt)
                             (.getBytes salt "UTF-8")
                             salt)))
       (loop [input (.digest digest (.getBytes password "UTF-8"))
              count n]
         (if (= count 0)
           (apply str input)
           (recur (do (.reset digest)
                      (.digest digest input))
                  (dec count)))))))

(defn login-controller [params session]
  (let [uniq (params :uniq)
        password (params :password)
        csrf (params :csrf)
        user (first (findEmployee {:uniq uniq}))]
    (dosync
        (debug "Authenticating" user)
        (if
          (and
            (not (nil? user))
            (= (hash-password password "outcastgeek") (user :password))
            (= uniq (user :uniq))
            (= csrf (session :csrf)))
          (do
            (debug "Authenticated!!!!")
            {:status 302
             :headers {"Location" "/"}
             :session (merge session {:login true :username (user :username)
                                      :user-info user
                                      :flash (str "You are now Logged In " (user :username))
                                      :flashstyle "alert-success"})
             })
          (do
            (debug "Cannot authenticate with username: " uniq " + password: **** combination")
            {:status 302
             :headers {"Location" "/login"}
             :session (merge session {:flash "Could not Log you in with the provided Information"
                                      :flashstyle "alert-error"})
             })))))

(defn register-controller [params session]
  (let [username (params :username)
        email (params :email)
        password (params :password)
        confirmpassword (params :confirmpassword)
        csrf (params :csrf)
        user (first (findExistingEmployee {:username username}))]
  (dosync
    (cond
      (and
        (= confirmpassword password)
        (not (= username password))
        (nil? user)
        ; Username can include letters, numbers,
        ; spaces, underscores, and hyphens.
        (.matches username "[\\w\\s\\-]+")
;        (.matches email "\\A[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@[A-Z0-9-]+(?:\\.[A-Z0-9-]+)*\\Z")
;        (.matches password "^.*(?=.{6,})(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d\\W]).*$")
        (= csrf (session :csrf)))
      (do
        (queueEmployeeCreation
                    {:username username
                     :email email
                     :uniq csrf
                     :password (hash-password password "outcastgeek")})
        {:status 302
         :headers {"Location" "/"}
         :session (merge session {:login true :username username
                                  ::user-info {:username username
                                               :email email
                                               :uniq csrf}
                                  :flash (str "You are now Registered " username)
                                  :flashstyle "alert-success"})
         })
      :else (do
              {:status 302
			         :headers {"Location" "/register"}
			         :session (merge session {:flash "Could not Register you with the provided Information"
			                                      :flashstyle "alert-error"})
			         })))))

(defn profile-controller [params session]
  (let [username (params :username)
        email (params :email)
        hashed-password (cond (nil? (params :password))
                              (-> session :user-info :password)
                         :else (hash-password (params :password) "outcastgeek"))
        password (params :password)
        confirmpassword (params :confirmpassword)
        firstname (params :first_name)
        lastname (params :last_name)
        csrf (params :csrf)
        uniq (-> session :user-info :uniq)]
  (debug "Profile update Data:" params)
  (if
    (and
      (= confirmpassword password)
      (not (= username password))
      ; Username can include letters, numbers,
      ; spaces, underscores, and hyphens.
      (.matches username "[\\w\\s\\-]+")
      ;        (.matches email "\\A[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@[A-Z0-9-]+(?:\\.[A-Z0-9-]+)*\\Z")
      ;        (.matches password "^.*(?=.{6,})(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d\\W]).*$")
      (= csrf (session :csrf)))
    (do
      (queueEmployeeUpdate
        {:username username
         :email email
         :first_name firstname
         :last_name lastname
         :uniq uniq
         :password hashed-password})
      (info "Successfully updated profile...")
      {:status 302
       :headers {"Location" "/profile"}
       :session (merge session {:login true :username username
                                :flash (str "Your profile has been updated " username)
                                :flashstyle "alert-success"})
       })
    (do
      (error "Failed to update profile... existing CSRF=" csrf)
      {:status 302
       :headers {"Location" "/profile"}
       :session (merge session {:flash "Could not Update your profile with the provided Information"
                                :flashstyle "alert-error"})
       }))
    ))

(def appUrl
  (str (appProperties :app-protocol)
       "://"
       (appProperties :app-url)
       ":"
       (appProperties :app-port)))

(def csrf
  (str (UUID/randomUUID)))

(def facebook-oauth2
  {:authorization-uri "https://graph.facebook.com/oauth/authorize"
   :access-token-uri "https://graph.facebook.com/oauth/access_token"
   :redirect-uri (str appUrl "/auth/login")
   :client-id (appProperties :fb-client)
   :client-secret (appProperties :fb-secret)
   :access-query-param :access_token
   :scope ["user_photos" "friends_photos"]
   :grant-type "authorization_code"})

;; redirect user to (:uri auth-req) afterwards
(def fb-auth-req
  (oauth2/make-auth-request facebook-oauth2 csrf))

(def google-oauth2
  {:authorization-uri "https://accounts.google.com/o/oauth2/auth"
   :access-token-uri "https://accounts.google.com/o/oauth2/token"
   :redirect-uri (str appUrl "/oauth2callback")
   :client-id (appProperties :goog-client)
   :client-secret (appProperties :goog-secret)
   :access-query-param :access_token
   :scope ["https://www.googleapis.com/auth/userinfo.email"
           "https://www.googleapis.com/auth/userinfo.profile"]
   :grant-type "authorization_code"})

;; redirect user to (:uri auth-req) afterwards
(def goog-auth-req
  (oauth2/make-auth-request google-oauth2 csrf))

(def dwolla-oauth2
  {:authorization-uri "https://www.dwolla.com/oauth/v2/authenticate"
   :access-token-uri "https://www.dwolla.com/oauth/v2/token"
   :redirect-uri (str appUrl "/oauth2cash")
   :client-id (appProperties :dwolla-client)
   :client-secret (appProperties :dwolla-secret)
   :access-query-param :access_token
   :scope ["balance|contacts|transactions|request|send|accountinfofull"]
   :grant-type "authorization_code"})

;; redirect user to (:uri auth-req) afterwards
(def dwolla-auth-req
  (oauth2/make-auth-request dwolla-oauth2))

(def instagram-oauth2
  {:authorization-uri "https://api.instagram.com/oauth/authorize"
   :access-token-uri "https://api.instagram.com/oauth/access_token"
   :redirect-uri (str appUrl "/instagramCallback")
   :client-id (appProperties :instagram-client)
   :client-secret (appProperties :instagram-secret)
   :access-query-param :access_token
   :scope ["relationships" "comments" "likes"]
   :grant-type "authorization_code"})

;; redirect user to (:uri auth-req) afterwards
(def instagram-auth-req
  (oauth2/make-auth-request instagram-oauth2))

(def live-oauth2
  {:authorization-uri "https://login.live.com/oauth20_authorize.srf"
   :access-token-uri "https://login.live.com/oauth20_token.srf"
   :redirect-uri (str appUrl "/liveCallback")
   :client-id (appProperties :live-client)
   :client-secret (appProperties :live-secret)
   :access-query-param :access_token
   :scope ["wl.signin" "wl.basic" "wl.offline_access"]
   :grant-type "authorization_code"
   :response-type "code"})

;; redirect user to (:uri auth-req) afterwards
(def live-auth-req
  (oauth2/make-auth-request live-oauth2 csrf))

(def foursquare-oauth2
  {:authorization-uri "https://foursquare.com/oauth2/authorize"
   :access-token-uri "https://foursquare.com/oauth2/access_token"
   :redirect-uri (str appUrl "/foursquareCallback")
   :client-id (appProperties :foursquare-client)
   :client-secret (appProperties :foursquare-secret)
   :access-query-param :access_token
   :scope ["photos" "badges" "checkins" "todos"]
   :grant-type "authorization_code"})

;; redirect user to (:uri auth-req) afterwards
(def foursquare-auth-req
  (oauth2/make-auth-request foursquare-oauth2 csrf))

(def git-oauth2
  {:authorization-uri "https://github.com/login/oauth/authorize"
   :access-token-uri "https://github.com/login/oauth/access_token"
   :redirect-uri (str appUrl "/gitCallback")
   :client-id (appProperties :git-client)
   :client-secret (appProperties :git-secret)
   :access-query-param :access_token
   :scope ["user" "repo" "public_repo" "delete_repo" "gist"]
   :grant-type "authorization_code"})

;; redirect user to (:uri auth-req) afterwards
(def git-auth-req
  (oauth2/make-auth-request git-oauth2))

(def paypal-oauth2
  {:authorization-uri "https://identity.x.com/xidentity/resources/authorize"
   :access-token-uri "https://identity.x.com/xidentity/oauthtokenservice"
   :redirect-uri (str appUrl "/paypalCallback")
   :client-id (appProperties :paypal-client)
   :client-secret (appProperties :paypal-secret)
   :access-query-param :access_token
   :scope ["https://identity.x.com/xidentity/resources/profile/me"]
   :grant-type "authorization_code"
   :response-type "code"})

;; redirect user to (:uri auth-req) afterwards
(def paypal-auth-req
  (oauth2/make-auth-request paypal-oauth2))

(def flattr-oauth2
  {:authorization-uri "https://flattr.com/oauth/authorize"
   :access-token-uri "https://flattr.com/oauth/token"
   :redirect-uri (str appUrl "/flattrCallback")
   :client-id (appProperties :flattr-client)
   :client-secret (appProperties :flattr-secret)
   :access-query-param :access_token
   :scope ["flattr" "thing" "email"]
   :grant-type "authorization_code"
   :response-type "code"})

;; redirect user to (:uri auth-req) afterwards
(def flattr-auth-req
  (oauth2/make-auth-request flattr-oauth2))

(def oauth-providers
  {:fb fb-auth-req
   :goog goog-auth-req
   :dwolla dwolla-auth-req
   :instagram instagram-auth-req
   :live live-auth-req
   :foursquare foursquare-auth-req
   :git git-auth-req
   :paypal paypal-auth-req
   :flattr flattr-auth-req})

(defmacro login-processor [provider-info-map provider-endpoint-info user-info-uri access-token-handle userinfo-function]
  `(fn [params# session#]
    (let [auth-resp# (select-keys params# [:code :state])
          access-info# (oauth2/get-access-token ~provider-info-map auth-resp# ~provider-endpoint-info)
          access-token# (access-info# :access-token)
          resp# (oauth2/get ~user-info-uri {:query-params {(keyword ~access-token-handle) access-token#}})
          user-data# (parse-string (resp# :body) true)
          user-info# (~userinfo-function user-data#)]
    (debug user-info#)
    (queueEmployeeCreation
                    (merge user-info# {:password (hash-password (user-info# :uniq) "outcastgeek")}))
      (do
        {:status 302
         :headers {"Location" "/"}
         :session (merge session# {:access-info access-info#
                                   :user-data user-data#
                                   :login true :username (user-info# :username)
                                   :user-info user-info#
                                   :flash (str "You are now Logged In " (user-info# :username))
                                   :flashstyle "alert-success"})
         })
    )))

(defn logout-controller [request]
  (do
    {:status 302
     :headers {"Location" "/"}
     :session {:flash "You are now Logged Out!"
               :flashstyle "alert-success"}
     }))

(defn auth-req? [req]
  (let [session (req :session)]
    (cond
     (not (session :username))
     false
     :else
     true)))

(defn auth-ses? [ses]
  (cond
   (not (ses :username))
   false
   :else
   true))

(defn gen-login-unless-auth [condition f]
  (fn [& args]
    (dosync
     (cond (do condition)
           (apply f args)
           :else (do
                   {:status 302
                    :headers {"Location" "/login"}
                    })))))
