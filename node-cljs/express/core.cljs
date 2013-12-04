(ns express.core
  (:require [cljs.nodejs :as node]
            [express.s3 :as s3]))

(def express (node/require "express"))
(def app (express))

(defn hello [req res]
  (doto res
    (.setHeader "X-Powered-By" "Blood, sweat, and tears")
    (.send "Hello World")))

(defn -main [& args]
  (doto app
    (.use (. express (favicon)))
    (.use (. express (logger "dev")))
    (.get "/" hello)
    (.listen 3000))
  (.log js/console (str "Express server started on port: " (.get app "port"))))

(set! *main-cli-fn* -main)
