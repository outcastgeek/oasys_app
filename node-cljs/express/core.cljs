(ns express.core
  (:require [cljs.nodejs :as node]
            [express.s3 :as s3]))

(def express (node/require "express"))
(def app (express))

(defn -main [& args]
  (doto app
    (.use (. express (favicon)))
    (.use (. express (logger "dev")))
    (.get "/" (fn [req res]
                (.send res "Hello World")))
    (.listen 3000))
  (.log js/console (str "Express server started on port: " (.get app "port"))))

(set! *main-cli-fn* -main)
