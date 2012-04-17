(ns hello
  (:use [jayq.core :only [$ css inner]])
  (:use-macros [crate.macros :only [defpartial]])
  (:require [crate.core :as crate]))

(defn ^:export greet [name]
  (str "Hello " name "!"))

(def $interface ($ :#interface))

(-> $interface
  (css {:background "blue"})
  (inner "Loading!"))

(defpartial header []
  [:header
   [:h1 "My app!"]])
