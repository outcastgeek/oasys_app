(ns com.outcastgeek.services.web.fluid
  (:use hiccup.core
        hiccup.page
        hiccup.def
        hiccup.element))

(defelem button-link-to
  "Wraps some content in a HTML button hyperlink with the supplied URL."
  [^String url ^String content]
  [:a {:href url :class "button"} content])

(defn input [^String name ^String xName session & {:keys [style size required]
                                                   :or {style ""
                                                        size "30"
                                                        required "required"}}]
  [:div {:class "clearfix"}
   [:label {:for xName} name]
   [:div {:class "input"} [:input {:id xName :name xName :value ((keyword xName) session)
                                   :style style :size size :required required :type "text"}]]])

(defn secret-input [^String name ^String xName session & {:keys [style size required]
                                                          :or {style ""
                                                               size "30"
                                                               required "required"}}]
  [:div {:class "clearfix"}
   [:label {:for xName} name]
   [:div {:class "input"} [:input {:id xName :name xName :value ((keyword xName) session)
                                   :style style :size size :required required :type "password"}]]])

(defn uneditable-input [^String value]
  [:div {:class "clearfix"}
   [:div {:class "input" :style "margin-left: 0px"} [:span {:class "uneditable-input"} value]]])

(defn style [rules]
  [:style {:type "text/css"} rules])

(defn flash [request]
  (let [flash (-> request :session :flash)
        flashstyle (-> request :session :flashstyle)]
    (cond
     (or (= flash "") (nil? flash))
      (str "<!-- flash goes here!!!!-->")
     :else
      (html
       [:div {:id "flash" :class (str "alert alert-block " flashstyle)}
        [:button {:class "close" :data-dismiss "alert"} "x"]
        [:p [:strong flash]]]))))

(defn popoverLeft [^String title ^String message]
  [:div {:class "well popover-well"}
   [:div {:class "popover left"}
    [:div {:class "arrow"}
     [:div {:class "inner"}
      [:h3 {:class "title"}
       title]
      [:div {:class "content"}
       [:p "Hello" message " from Clojure!!!!"]]]]]])

(defn goog-analytics []
   (javascript-tag
"
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-21218897-1']);
  _gaq.push(['_setDomainName', 'upgradeavenue.com']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
"
   ))

(defn html-doc
  [request  ^String title body]
  (let [session (request :session)]
  (html
    (str "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML+RDFa 1.0//EN' 'http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd'>")
    [:html
    {:class "no-js"
     :xmlns "http://www.w3.org/1999/xhtml"
     :version "XHTML+RDFa 1.0"
     "xml:lang" "en"
     "xmlns:rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     "xmlns:rdfs" "http://www.w3.org/2000/01/rdf-schema#"
     "xmlns" "http://www.w3.org/1999/xhtml"
     "xmlns:foaf" "http://xmlns.com/foaf/0.1/"
     "xmlns:gr" "http://purl.org/goodrelations/v1#"
     "xmlns:xsd" "http://www.w3.org/2001/XMLSchema#"
     "xmlns:ng" "http://angularjs.org"
     :ng-app "main"}
     [:head
      [:title (str title "Web App")]
      [:meta {:http-equiv "Content-Type"
              :content "text/html; charset=UTF-8"}]
      [:meta {:http-equiv "X-UA-Compatible"
              :content "chrome=1"}]
      [:link {:rel "shortcut icon"
              :href "/static/images/favicon.ico"}]
      (str
        "
        <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
        <!--[if lt IE 8]>
          <script src='http://html5shim.googlecode.com/svn/trunk/html5.js'></script>
        <![endif]-->
        ")
      (include-css "/static/stylesheets/bootstrap.min.css")
      (include-css "/static/stylesheets/bootstrap-responsive.min.css")
      [:style {:type "text/css"}
       "body {
          //padding-top: 60px;
          padding-bottom: 40px;
        }
       "]
      ]
     [:body {:ng-controller "MainCtrl"}
      [:div {:class "navbar" :data-dropdown "dropdown"}
       [:div {:class "navbar-inner"}
        [:div {:class "container"}
         [:a {:class "brand" :href "/" :style "padding-top: 2px; padding-bottom: 2px;"}
             [:img {:src "/static/images/oasys-logo9.png" :alt "Oasys Technologies" :height "30px"}]
          ]
         [:ul {:class "nav nav-pills" :style "margin-top: 3px;"}
          ;[:li (button-link-to "" (str ">>"title"<<"))]
          [:li (button-link-to "/resume" "Resume Builder")]
          [:li (button-link-to "/about" "About")]
          [:li {:class "divider-vertical"}]
          [:li {:class "dropdown" :id "Contact"}
            [:a {:class "dropdown-toggle" :data-toggle "dropdown" :href "#Contact"}
             "Contact"
             [:b {:class "caret"}]]
            [:ul {:class "dropdown-menu"}
              [:li [:a {:href "http://www.twitter.com/akpanydre" :target "_blank"} "Twitter"]]
              [:li [:a {:href "http://www.linkedin.com/in/ebbybenjamin" :target "_blank"} "LinkedIn"]]
              [:li [:a {:href "http://www.outcastgeek.com/" :target "_blank"} "Blog"]]
            ]]
          [:li {:class "dropdown" :id "SearchBy"}
           [:a {:class "dropdown-toggle" :data-toggle "dropdown" :href "#SearchBy"}
            "SearchBy"
            [:b {:class "caret"}]]
           [:ul {:class "dropdown-menu"}
            [:li [:a {:href "#" :ng-click "searchBy('service')"} "Service"]]
            [:li [:a {:href "#" :ng-click "searchBy('lob')"} "LOB"]]
            [:li [:a {:href "#" :ng-click "searchBy('source')"} "Source"]]
            ]]
          ]
         (dosync
		       (cond
		         (not (session :username))
              (html
               [:div {:class "btn-group pull-right"}
                [:a {:class "btn dropdown-toggle" :data-toggle "dropdown"}
                 [:i {:class "icon-user"}] "Account"
                 [:span {:class "caret"}]]
                [:ul {:class "dropdown-menu"}
                 [:li [:a {:href "/login"} "Login"]]
                 [:li [:a {:href "/register"} "Register"]]
                 [:li [:a {:href "#Profile"} "Profile"]]
                 [:li {:class "divider"}]
                 [:li [:a {:href "/login/fb"} "Facebook"]]
                 [:li [:a {:href "/login/goog"} "Google+"]]
                 [:li [:a {:href "/login/dwolla"} "Dwolla"]]
                 [:li [:a {:href "/login/paypal"} "Paypal"]]
                 [:li [:a {:href "/login/instagram"} "Instagram"]]
                 [:li [:a {:href "/login/live"} "Windows Live ID"]]
                 [:li [:a {:href "/login/foursquare"} "Foursquare"]]
                 [:li [:a {:href "/login/git"} "Github"]]]])
		         (= (session :username) "admin")
		           (html
                [:ul {:class "nav nav-pills pull-right" :style "margin-top: 3px;"}
                 [:li (button-link-to "/#Admin" "Admin")]
                 [:li {:class "divider-vertical"}]
                 [:li (button-link-to "/logout" "Logout")]])
		         :else
               (html
                 [:ul {:class "nav nav-pills pull-right" :style "margin-top: 3px;"}
                  [:li (button-link-to "#Profile" (session :username))]
                  [:li {:class "divider-vertical"}]
                  [:li (button-link-to "/logout" "Logout")]]
                 )))]]]
      [:div {:class "container"}
       [:div {:class "content"}
       (flash request)
       body
       [:footer
        [:p {:id "copyright"} "Copyright &copy; 2012  &bull; "
          [:a {:href "http://www.outcastgeek.com"} "outcastgeek"]
          " &bull;  All rights reserved"]]]]
       (include-js "http://yui.yahooapis.com/3.5.1/build/yui/yui-min.js")
       (include-js "/static/javascript/outcastgeek/common.js")
      ]])
))