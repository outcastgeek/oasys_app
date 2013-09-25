(ns repls.brepl
  (:require [cljs.repl.browser :as browser]
            [cemerick.piggieback :as pback]))

(defn run-b-repl []
  (pback/cljs-repl
  :repl-env (browser/repl-env :port 8090 :working-dir "target")))
