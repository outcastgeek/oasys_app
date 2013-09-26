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
                 [com.cemerick/piggieback "0.1.0"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [domina "1.0.1"]
                 [enfocus "2.0.0-beta2"]
                 [hiccup "1.0.4"]
                 [jayq "2.4.0"]
                 [crate "0.2.4"]]
  :aot :all
  :plugins [[lein-cljsbuild "0.3.3"]]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {
              :builds [
              {:id "brepl"
               :source-paths ["src-cljs/repls"]
               :compiler {
                           :output-to "oasysusa/oasysusa/static/js/repl.js"
                           :optimizations :simple}}
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
