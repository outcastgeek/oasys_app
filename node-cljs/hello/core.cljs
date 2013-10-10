(ns hello.core)

(defn -main [& args]
  (println (apply str (map [\space "world" "hello"] [2 0 1])))
  (let [http (js/require "http")
        handler (fn [req res] (.end res "hello sailor!"))
        server (.createServer http handler)]
    (.listen server 1337)))

(set! *main-cli-fn* -main)
