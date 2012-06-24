(ns crate.myapp
  (:use-macros [crate.def-macros :only [defpartial]])
  (:require [crate.core :as crate]))

(crate/html [:p.woot {:id "blah"} "Hey!"])