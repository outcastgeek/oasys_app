(ns repls.brepl-client
  (:require [clojure.browser.repl :as repl]))

(repl/connect "http://localhost:8090/repl")

