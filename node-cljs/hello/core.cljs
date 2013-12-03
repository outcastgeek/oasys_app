;(ns hello.core)
;
;(defn -main [& args]
;  (println (apply str (map [\space "world" "hello"] [2 0 1])))
;  (let [http (js/require "http")
;        handler (fn [req res] (.end res "hello sailor!"))
;        server (.createServer http handler)]
;    (.listen server 1337)))
;
;(set! *main-cli-fn* -main)

(ns hello.core
  (:require [cljs.nodejs :as nodejs]))

(def http (nodejs/require "http"))

(defn greetHandler [req res]
  (doto res
    (.writeHead 200 {"Content-Type" "text/plain"})
    (.end "Hello World")))

(defn server [handler port url]
  (-> (.createServer http handler)
    (.listen port url)))

(defn -main [& mess]
  (server greetHandler 1337 "127.0.0.1")
  (println "Server running at http://127.0.0.1:1337/"))

(set! *main-cli-fn* -main)

