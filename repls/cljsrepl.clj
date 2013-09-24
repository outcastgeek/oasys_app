
;;;; programmatically start browser repl

(ns cljsrepl
  (:require [cljs.repl :as repl]
            [cljs.repl.browser :as browser] ;; require the browser implementation of IJavaScriptEnv
            ))

(def env (browser/repl-env :port 8090 :working-dir "target")) ;; create a new environment
(repl/repl env) ;; start the REPL

(comment
  (.log js/console "lb")
  (.log js/console "rb")
  )

(ns enhanced.cljsrepl
  (:require [cljs.repl.browser :as browser]
            ;[clojure.tools.nrepl.server :as server]
            [cemerick.piggieback :as pback])
)

;(server/start-server
;  :handler (server/default-handler #'pback/wrap-cljs-repl)
;  ; ...additional `start-server` options as desired
;  :port 8090 :working-dir "target"
;  )

(pback/cljs-repl
  :repl-env (browser/repl-env :port 8090 :working-dir "target"))
