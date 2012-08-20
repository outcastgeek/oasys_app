(ns com.outcastgeek.services.web.fluid-test
  (:use [clojure.test :only [deftest is run-tests]]
        com.outcastgeek.services.web.fluid))

(deftest test-button-link-to
  (is (=
       [:a {:href "/a", :class "button"} "haha"]
       (button-link-to "/a" "haha"))
      ))

(deftest test-uneditable-input
  (is (=
       [:div
        {:class "clearfix"}
        [:div
         {:class "input", :style "margin-left: 0px"}
         [:span {:class "uneditable-input"} "Voila"]]]
       (uneditable-input "Voila"))
      ))

(deftest test-style
  (is (=
       [:style {:type "text/css"} "bogus"]
       (style "bogus"))
      ))


(deftest test-popoverLeft
  (is (=
       [:div
        {:class "well popover-well"}
        [:div
         {:class "popover left"}
         [:div
          {:class "arrow"}
          [:div
           {:class "inner"}
           [:h3 {:class "title"} "Titre"]
           [:div
            {:class "content"}
            [:p "Hello" "Message" " from Clojure!!!!"]]]]]]
       (popoverLeft "Titre" "Message")
      )))

;(run-tests)