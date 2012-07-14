(ns com.outcastgeek.config.AuthAuth
  (:use clojure.tools.logging
        somnium.congomongo
        com.outcastgeek.config.AppConfig)
  (:require [ring.middleware.session :as rs]
            [clj-oauth2.client :as oauth2]
            [clojure.data.json :as json])
  (:import java.util.UUID))

(set-connection! mongo-connection)

(defrecord User [username email password])

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
  (let [username (params :username)
        password (params :password)
        csrf (params :csrf)
        user (fetch-one :users
                        :where {:username username})]
    (dosync
        (debug "Authenticating...")
        (if
          (and
            (not (nil? user))
            (= (hash-password password "outcastgeek") (user :password))
            (= username (user :username))
            (= csrf (session :csrf)))
          (do
            (debug "Authenticated!!!!")
            {:status 302
             :headers {"Location" "/"}
             :session (merge session {:login true :username username
                                      :flash (str "You are now Logged In " username)
                                      :flashstyle "alert-success"})
             })
          (do
            (debug "Cannot authenticate with username: " username " + password: **** combination")
            {:status 302
             :headers {"Location" "/login"}
             :session (merge session {:flash "Could not Log you in with the provided Information"
                                      :flashstyle "alert-error"})
             })))))

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
   :grant-type "authorization_code"})

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

(def oauth-providers
  {:fb fb-auth-req
   :goog goog-auth-req
   :dwolla dwolla-auth-req
   :instagram instagram-auth-req
   :live live-auth-req
   :foursquare foursquare-auth-req
   :git git-auth-req
   :paypal paypal-auth-req})

(defmacro login-processor [provider-info-map provider-endpoint-info user-info-uri access-token-handle username-function]
  `(fn [params# session#]
    (let [auth-resp# (select-keys params# [:code :state])
          access-info# (oauth2/get-access-token ~provider-info-map auth-resp# ~provider-endpoint-info)
          access-token# (access-info# :access-token)
          resp# (oauth2/get ~user-info-uri {:query-params {(keyword ~access-token-handle) access-token#}})
          user-info# (json/read-json (resp# :body))
          username# (~username-function user-info#)]
    (debug user-info#)
    (do
      {:status 302
       :headers {"Location" "/"}
       :session (merge session# {:access-info access-info#
                                :user-info user-info#
                                :login true :username username#
                                :flash (str "You are now Logged In " username#)
                                :flashstyle "alert-success"})
       }))))

(defn logout-controller []
  (do
    {:status 302
     :headers {"Location" "/"}
     :session {:flash "You are now Logged Out!"
               :flashstyle "alert-success"}
     }))

(defn register-controller [params session]
  (let [username (params :username)
        email (params :email)
        password (params :password)
        confirmpassword (params :confirmpassword)
        csrf (params :csrf)
        user (fetch-one :users
                        :where {:username username})]
  (dosync
    (if
      (and
        (= confirmpassword password)
        (not (= username password))
        (nil? user)
        ; Username can include letters, numbers,
        ; spaces, underscores, and hyphens.
        (.matches username "[\\w\\s\\-]+")
        ;(.matches email "\\A[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@[A-Z0-9-]+(?:\\.[A-Z0-9-]+)*\\Z")
        ;(.matches password "^.*(?=.{6,})(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d\\W]).*$")
        (= csrf (session :csrf)))
      (do
        (insert! :users (User. username email (hash-password password "outcastgeek")))
        {:status 302
         :headers {"Location" "/"}
         :session (merge session {:login true :username username
                                  :flash (str "You are now Registered " username)
                                  :flashstyle "alert-success"})
         })
      (do
        {:status 302
         :headers {"Location" "/register"}
         :session (merge session {:flash "Could not Register you with the provided Information"
                                      :flashstyle "alert-error"})
         })))))

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
