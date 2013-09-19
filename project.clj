(defproject oasysusa "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src-clj"]
  :test-paths ["test-clj"]
  :resource-paths ["resource-clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1889"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [com.novemberain/validateur "1.5.0"]
                 [hiccup "1.0.4"]
                 [jayq "2.4.0"]
                 [crate "0.2.4"]]
  :plugins [[lein-cljsbuild "0.3.3"]]
  :cljsbuild {
              :builds [
              {:id "profile"
                        :source-paths ["src-cljs/profile"]
                        :externs ["externs/jquery-2.0.3.js" "externs/angular-1.0.7.js" "externs/angular-resource-1.0.7.js"]
                        :compiler {
;                                   :output-dir "oasysusa/oasysusa/static/js"
                                   :output-to "oasysusa/oasysusa/static/js/profile.js"
;                                   :source-map "oasysusa/oasysusa/static/js/profile.js.map"
                                   :optimizations :simple
                                   :pretty-print true}}
              ]}
  :repositories {"clojars.org" "http://clojars.org/repo"
                 "java.net" "http://download.java.net/maven/2"
                 "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
