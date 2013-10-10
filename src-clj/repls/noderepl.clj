(ns repls.noderepl
  (:require [cljs.repl :as repl]
            [cljs.repl.node :as node]))

(defn run-node-repl []
  (repl/repl (node/repl-env)))

(comment
  (run-node-repl)
  )
