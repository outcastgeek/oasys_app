(ns com.outcastgeek.services.web.resumebuilder
  (:use clojure.tools.logging
        hiccup.core
        hiccup.page
        somnium.congomongo
        com.outcastgeek.config.AppConfig
        com.outcastgeek.services.web.fluid
        com.outcastgeek.util.Macros
        [clj-style.core :as cs]
        com.outcastgeek.services.web.style))

(set-connection! mongo-connection)

(defprotocol StageValidator
  (validate [this] "Validates this stage")
  (update [this map] "Updates this stage")
  (renderStage [this] "Renders this stage"))

(defn isNotEmpty [params key]
  (= (= (key params) "") false))

(defrecord firstStage [p_xName p_xAddress p_xCityStateZip p_xPhone p_xEmail]
  StageValidator
  (validate [this]
    (and
      (isNotEmpty this :p_xName)
      (isNotEmpty this :p_xAddress)
      (isNotEmpty this :p_xCityStateZip)
      (isNotEmpty this :p_xPhone)
      (isNotEmpty this :p_xEmail)))
  (update [this map]
    (merge this (select-keys map [:p_xName :p_xAddress :p_xCityStateZip
                                  :p_xPhone :p_xEmail])))
  (renderStage [this]
          (html
            (input "Name" "p_xName" this :style "width: 100%" :size "")
            (input "Address" "p_xAddress" this :style "width: 100%" :size "")
            (input "City, State, Zip" "p_xCityStateZip" this :style "width: 100%" :size "")
            (input "Phone" "p_xPhone" this :style "width: 100%" :size "")
            (input "Email" "p_xEmail" this :style "width: 100%" :size ""))))

(defn firstStageData [raw]
  (-> (empty-record com.outcastgeek.services.web.resumebuilder.firstStage)
      (update raw)))

(defrecord secondStage [e_xDegree e_xSchool
                        e_xCityState e_xMajors e_xMinors]
  StageValidator
  (validate [this]
    (and
      ;(isNotEmpty this :e_xMinors) ;Not Mandatory?!?!?!?!
      (isNotEmpty this :e_xDegree)
      (isNotEmpty this :e_xSchool)
      (isNotEmpty this :e_xCityState)
      (isNotEmpty this :e_xMajors)))
  (update [this map]
    (merge this (select-keys map [:e_xDegree :e_xSchool :e_xCityState
                                  :e_xMajors :e_xMinors])))
  (renderStage [this]
          (html
            (uneditable-input (:e_xDegree this))
            (uneditable-input (:e_xSchool this))
            (uneditable-input (:e_xCityState this))
            (uneditable-input (:e_xMajors this))
            (uneditable-input (:e_xMinors this)))))

(defn secondStageData [collection]
  (map (fn [stageData]
           (-> (empty-record com.outcastgeek.services.web.resumebuilder.secondStage)
             (update stageData))) collection))

(defrecord thirdStage [j_xName j_xPosition j_xAddress
                       j_xCityState j_xStartDate j_xEndDate
                       xAccomplishment1 xAccomplishment2 xAccomplishment3]
  StageValidator
  (validate [this]
    ;(isNotEmpty this :j_xAccomplishment1) ;Not Mandatory?!?!?!?!
    ;(isNotEmpty this :j_xAccomplishment2) ;Not Mandatory?!?!?!?!
    ;(isNotEmpty this :j_xAccomplishment3) ;Not Mandatory?!?!?!?!
    (isNotEmpty this :j_xName)
    (isNotEmpty this :j_xPosition)
    (isNotEmpty this :j_xAddress)
    (isNotEmpty this :j_xCityState)
    (isNotEmpty this :j_xStartDate)
    (isNotEmpty this :j_xEndDate))
  (update [this map]
    (merge this (select-keys map [:j_xPosition :j_xCompany :j_xCityState :j_xStartDate :j_xEndDate
                                  :j_xAccomplishment1 :j_xAccomplishment2 :j_xAccomplishment3])))
  (renderStage [this]
          (html
            (uneditable-input (:j_xPosition this))
            (uneditable-input (:j_xCompany this))
            (uneditable-input (:j_xCityState this))
            (uneditable-input (:j_xStartDate this))
            (uneditable-input (:j_xEndDate this))
            (uneditable-input (:j_xAccomplishment1 this))
            (uneditable-input (:j_xAccomplishment2 this))
            (uneditable-input (:j_xAccomplishment3 this)))))

(defn thirdStageData [collection]
  (map (fn [stageData]
           (-> (empty-record com.outcastgeek.services.web.resumebuilder.thirdStage)
             (update stageData))) collection))

(defn embeddedResume [session]
  (html
;    (style (cs/render bogusStyleForName))
    [:h1 {:id "name"} (-> session :firstStage :p_xName)]

    [:div {:id "personal" :style (cs/render (p-info))}
      [:span {:style (cs/render (blue))} (:p_xAddress session)]
      [:span (:p_xCityStateZip session)]
      [:span (:p_xPhone session)]
      [:span (:p_xEmail session)]
    ]
    [:div {:id "education"}
      [:h2 {:style (cs/render (resume-section-title))} "Education"]
      [:div {:class "edu"}
        [:p (:e_xDegree session)]
        [:p (:e_xSchool session)]
        [:p (:e_xCityState session)]
        [:p (:e_xMajors session)]
        [:p (:e_xMinors session)]
      ]]
    [:div {:id "jobs"}
      [:h2 {:style (cs/render (resume-section-title))} "Jobs"]
      [:div {:class "job"}
        [:p (:j_xPosition session)]
        [:p (:j_xCompany session)]
        [:p (:j_xCityState session)]
        [:p (:j_xStartDate session)]
        [:p (:j_xEndDate session)]
        [:p (:j_xAccomplishment1 session)]
        [:p (:j_xAccomplishment2 session)]
        [:p (:j_xAccomplishment3 session)]
      ]
    ]))

(defn fullResume [session]
  (html
    [:html
     [:head
      [:title "Your Resume"]]
     [:body (embeddedResume session)]]
    ))

(defn resume-get-step [step csrf session & {:keys [action index]
                                                   :or {action ""
                                                        index ""}}]
  (let [stageOne (if-let [stg1 (firstStageData (:firstStage session))] stg1 (empty-record com.outcastgeek.services.web.resumebuilder.firstStage))
        stageTwo (if-let [list2 (secondStageData (:secondStage session))] list2 (list (empty-record com.outcastgeek.services.web.resumebuilder.secondStage)))
        stageThree (if-let [list3 (thirdStageData (:thirdStage session))] list3 (list (empty-record com.outcastgeek.services.web.resumebuilder.thirdStage)))]
    (cond
      (= step "1")
      (html
        [:ul {:class "breadcrumb"}
         [:li {:class "active"} [:a {:href "/resume/1"} "Stage 1"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/2"} "Stage 2"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/3"} "Stage 3"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/4"} "Stage 4"] [:span {:class "divider"}]]]
        [:div {:class "page-header"}
         [:h1 "It's time to start your resume! Let's get started."
          [:br]
          [:small "Your mission, should you choose to accept it, is to work through 4 steps like a boss."]]]
        [:div {:class "row"}
         [:div {:class "span16"}
          ;        [:h2 "The journey of a thousand leagues begins with a step! Good luck."]
          [:h2 "Step 1. The Basics"]
          [:br]
          [:form {:method "post" :action "/resume" :enctype "application/x-www-form-urlencoded"}
           [:fieldset
            ;[:legend "Who are you?"]
            [:div {:class "row"}
             [:div {:class "span8"}
              (.renderStage stageOne)]
             [:div {:class "span8"}
              (uneditable-input "Tyler Durden")
              (uneditable-input "123 Paper Street")
              (uneditable-input "Minneapolis, MN 55407")
              (uneditable-input "332-245-5533")
              (uneditable-input "tyler.durden@upgradeavenue.com")]]]
           [:input {:type "hidden" :name "step" :value step}]
           [:input {:type "hidden" :name "csrf" :value csrf}]
           [:div {:class "actions"}
            [:input {:class "btn btn-primary" :type "submit" :value "Save & Continue"}]
            [:button {:class "btn" :type "reset"} "Cancel"]]
           ]]]
        (goog-analytics)
        )
      (= step "2")
      (html
        [:ul {:class "breadcrumb"}
         [:li [:a {:href "/resume/1"} "Stage 1"] [:span {:class "divider"}]]
         [:li {:class "active"} [:a {:href "/resume/2"} "Stage 2"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/3"} "Stage 3"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/4"} "Stage 4"] [:span {:class "divider"}]]]
        [:div {:class "page-header"}
         [:h1 "Ah, there you are!"
          [:br]
          [:small "Congratulations! You made it to the 2nd level. Now tell us what schools you've attended?"]]]
        [:div {:class "row"}
         [:div {:class "span16"}
          [:h2 "Step 2. Education"]
          [:br]
          [:form {:method "post" :action "/resume" :enctype "application/x-www-form-urlencoded"}
           [:fieldset
            ;[:legend "College and High School"]
            [:div {:class "row"}
             [:div {:class "span8"}
              (map (fn [stageDataItem]
                     (when
                       (.validate stageDataItem)
                       (.renderStage stageDataItem))) stageTwo)
              (input "Degree" "e_xDegree" {} :style "width: 100%" :size "")
              (input "School" "e_xSchool" {} :style "width: 100%" :size "")
              (input "City, State" "e_xCityState" {} :style "width: 100%" :size "")
              (input "Majors" "e_xMajors" {} :style "width: 100%" :size "")
              (input "Minors" "e_xMinors" {} :style "width: 100%" :size "")]
             [:div {:class "span8"}
              (uneditable-input "Bachelor's of Science")
              (uneditable-input "Something University")
              (uneditable-input "St Cloud, MN 55411")
              (uneditable-input "Finance, Accounting, Chemistry")
              (uneditable-input "Psychology")]]]
           [:input {:type "hidden" :name "step" :value step}]
           [:input {:type "hidden" :name "csrf" :value csrf}]
           [:div {:class "actions"}
            [:input {:class "btn btn-primary" :type "submit" :value "Save & Continue"}]
            [:button {:class "btn" :type "reset"} "Add another school"]]
           ]]]
        (goog-analytics))
      (= step "3")
      (html
        [:ul {:class "breadcrumb"}
         [:li [:a {:href "/resume/1"} "Stage 1"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/2"} "Stage 2"] [:span {:class "divider"}]]
         [:li {:class "active"} [:a {:href "/resume/3"} "Stage 3"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/4"} "Stage 4"] [:span {:class "divider"}]]]
        [:div {:class "page-header"}
         [:h1 "Ah, there you are!"
          [:br]
          [:small "Excellent! You're doing great. Now list out the jobs you've had."]]]
        [:div {:class "row"}
         [:div {:class "span16"}
          [:h2 "Step 3. Jobs, Internships, Extra-curricular Activities"]
          [:br]
          [:form {:method "post" :action "/resume" :enctype "application/x-www-form-urlencoded"}
           [:fieldset
            ;[:legend "Jobs, Internships, Leadership Experience"]
            [:div {:class "row"}
             [:div {:class "span8"}
              (map (fn [stageDataItem] (when
                       (.validate stageDataItem)
                       (.renderStage stageDataItem))) stageThree)
              (input "Position" "j_xPosition" {} :style "width: 100%" :size "")
              (input "Company" "j_xCompany" {} :style "width: 100%" :size "")
              (input "City, State" "j_xCityState" {} :style "width: 100%" :size "")
              (input "Start Date" "j_xStartDate" {} :style "width: 100%" :size "")
              (input "End Date" "j_xEndDate" {} :style "width: 100%" :size "")
              (input "Accomplishment #1" "j_xAccomplishment1" {} :style "width: 100%" :size "")
              (input "Accomplishment #2" "j_xAccomplishment2" {} :style "width: 100%" :size "")
              (input "Accomplishment #3" "j_xAccomplishment3" {} :style "width: 100%" :size "")]
             [:div {:class "span8"}
              (uneditable-input "Founder")
              (uneditable-input "Fight Club")
              (uneditable-input "Seattle, WA 65307")
              (uneditable-input "June 2010")
              (uneditable-input "Present")]]]
           [:input {:type "hidden" :name "step" :value step}]
           [:input {:type "hidden" :name "csrf" :value csrf}]
           [:div {:class "actions"}
            [:input {:class "btn btn-primary" :type "submit" :value "Save & Continue"}]
            [:button {:class "btn" :type "reset"} "Add another job"]]
           ]]]
        (goog-analytics))
      (= step "4")
      (html
        [:ul {:class "breadcrumb"}
         [:li [:a {:href "/resume/1"} "Stage 1"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/2"} "Stage 2"] [:span {:class "divider"}]]
         [:li [:a {:href "/resume/3"} "Stage 3"] [:span {:class "divider"}]]
         [:li {:class "active"} [:a {:href "/resume/4"} "Stage 4"] [:span {:class "divider"}]]]
        [:div {:class "page-header"}
         [:h1 [:span (str "Awesome job " (-> session :firstStage :p_xName) "! Here is your resume!!!")]
          [:br]
          [:small "This is your dashboard."]]]
        [:div {:class "row"}
         [:div {:class "span10"}
          ;[:h2 "Your resume preview"]  -- Making the design cleaner. This looked odd.
          ;[:br]
          (embeddedResume session)]
         [:div {:class "span6"}
          [:h3 "Actions"]
          [:a {:class "btn btn-large btn-primary" :href "/downloadResume"} "Download as PDF"]
          [:a {:class "btn btn-large" :href "/resume/1"} "Edit resume"]]]
        (goog-analytics))
      :else
      (html
        [:div {:class "hero-unit"}
         [:h1 "Less stress. More success!"]
         [:p "It's like the 'TurboTax for your resume'. You just fill in some details, and we do the magic behind the scenes.
           Get started quickly. Make continous improvements. Don't worry about formatting, typos. That's what we call building a resume.... like a boss!"]
         [:p [:a {:class "btn btn-primary btn-large"  :href "/resume/1"} "Learn more &raquo;"]]]
        [:div {:class "row"}
         [:div {:class "span5"}
          [:h2 "Free"]
          [:p "Free as in 'Free Pizza' or 'Free Beer'. We get it, you're busy and let's be honest, broke.
            We've been through those weeks of Ramen noodles and checking out campus events for free food. Mmmm.... free food!"]
          [:p [:a {:class "btn" :href "/resume/1"} "View details &raquo;"]]]
         [:div {:class "span5"}
          [:h2 "Easy"]
          [:p "We would say 'so easy a caveman could do it', but we probably can't due to legal reasons... but you get the point. Easy! Did we mention free yet?"]
          [:p [:a {:class "btn" :href "/resume/1"} "View details &raquo;"]]]
         [:div {:class "span6"}
          [:h2 "Awesome"]
          [:p "Less stress. More fun. More success. I guess you could say . Duis mollis, est non commodo luctus, nisi erat porttitor ligula, eget lacinia odio sem nec elit."]
          [:p [:a {:class "btn" :href "/resume/1"} "View details &raquo;"]]]
         ]
        (goog-analytics))
      )))

(defn resume-handler [params session]
  (let [step (params :step)
        csrf (params :csrf)
        stageOne (-> (empty-record com.outcastgeek.services.web.resumebuilder.firstStage)
                   (update params))
        stageTwoList (if-let [list2 (:secondStage session)] list2 ())
        stageTwo (-> (empty-record com.outcastgeek.services.web.resumebuilder.secondStage)
                   (update params))
        stageThreeList (if-let [list3 (:thirdStage session)] list3 ())
        stageThree (-> (empty-record com.outcastgeek.services.web.resumebuilder.thirdStage)
                     (update params))]
    (dosync
      (debug "Collecting step#" (str step) " data...")
      (cond
        (= csrf (session :csrf))
        (cond
          (and
            (= step "1")
            (.validate stageOne))
          (do
            (debug "Cannot process CSRF requests!")
            {:status 302
             :headers {"Location" (str "/resume/" (+ (Integer/parseInt step) 1))}
             :session (merge session {:flash (str (params :p_xName) ", you completed stage #" step)
                                      :flashstyle "alert-success"
                                      :firstStage (select-keys
                                                    stageOne [:p_xName :p_xAddress
                                                              :p_xCityStateZip
                                                              :p_xPhone :p_xEmail])})
             })
          (and
            (= step "2")
            (.validate stageTwo))
          (do
            (debug "Cannot process CSRF requests!")
            {:status 302
             :headers {"Location" (str "/resume/" (+ (Integer/parseInt step) 1))}
             :session (merge session {:flash (str (-> session :firstStage :p_xName) ", you completed stage #" step)
                                      :flashstyle "alert-success"
                                      :secondStage (conj stageTwoList
                                                         (select-keys
                                                           stageTwo [:e_xDegree :e_xSchool
                                                                     :e_xCityState :e_xMajors
                                                                     :e_xMinors]))})
             })
          (and
            (= step "3")
            (.validate stageThree)
            )
          (do
            (debug "Cannot process CSRF requests!")
            {:status 302
             :headers {"Location" (str "/resume/" (+ (Integer/parseInt step) 1))}
             :session (merge session {:flash (str (-> session :firstStage :p_xName) ", you completed stage #" step)
                                      :flashstyle "alert-success"
                                      :thirdStage (conj stageThreeList
                                                        (select-keys
                                                          stageThree [:j_xPosition :j_xCompany
                                                                      :j_xCityState :j_xStartDate
                                                                      :j_xEndDate :j_xAccomplishment1
                                                                      :j_xAccomplishment2
                                                                      :j_xAccomplishment3]))})
             })
          :else
          (do
            (debug "Invalid data submitted!")
            {:status 302
             :headers {"Location" (str "/resume/" step)}
             :session (merge session {:flash "Invalid data submitted!"
                                      :flashstyle "alert-error"})
             }))
        :else
          (do
            (debug "Cannot process CSRF requests!")
            {:status 302
             :headers {"Location" (str "/resume/" step)}
             :session (merge session {:flash "Unauthorized request!"
                                    :flashstyle "alert-warning"})
             }))
        )))
