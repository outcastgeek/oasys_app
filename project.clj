(defproject oasysusa "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src-clj"]
  :test-paths ["test-clj"]
  :resource-paths ["resource-clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2014"]
                 [com.cemerick/piggieback "0.1.2"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [org.clojure/google-closure-library-third-party "0.0-2029-2"]
                 [domina "1.0.2"]
                 [enfocus "2.0.0-beta2"]
                 [hiccup "1.0.4"]
                 [jayq "2.4.0"]
                 [crate "0.2.4"]
                 [org.bodil/cljs-noderepl "0.1.10"]]
  :aot :all :plugins [[lein-cljsbuild "0.3.4"]
                      [org.bodil/lein-noderepl "0.1.10"]]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {
               :builds [
                         {:id "brepl"
                          :source-paths ["src-cljs/repls"]
                          :compiler {
                                      :output-to "oasysusa/oasysusa/static/js/repl.js"
                                      :optimizations :simple
                                      :pretty-print true}}
                         {:id "profile-dev"
                          :source-paths ["src-cljs/profile", "src-cljs/utils"]
                          :externs ["externs/jquery-1.9.js"]
                          :compiler {
                                      :output-to "oasysusa/oasysusa/static/js/profile-dev.js"
                                      :optimizations :simple
                                      :pretty-print true}}
                         {:id "profile"
                          :source-paths ["src-cljs/profile", "src-cljs/utils"]
                          :externs ["externs/jquery-1.9.js"]
                          :compiler {
;                                      :output-dir "oasysusa/oasysusa/static/js"
                                      :output-to "oasysusa/oasysusa/static/js/profile.js"
                                      :source-map "oasysusa/oasysusa/static/js/profile.js.map"
                                      :optimizations :advanced
                                      :pretty-print false}}
                         {:id "project-dev"
                          :source-paths ["src-cljs/timesheet/project", "src-cljs/utils"]
                          :externs ["externs/jquery-1.9.js"]
                          :compiler {
                                      :output-to "oasysusa/oasysusa/static/js/project-dev.js"
                                      :optimizations :simple
                                      :pretty-print true}}
                         {:id "project"
                          :source-paths ["src-cljs/timesheet/project", "src-cljs/utils"]
                          :externs ["externs/jquery-1.9.js"]
                          :compiler {
;                                      :output-dir "oasysusa/oasysusa/static/js"
                                      :output-to "oasysusa/oasysusa/static/js/project.js"
                                      :source-map "oasysusa/oasysusa/static/js/project.js.map"
                                      :optimizations :advanced
                                      :pretty-print false}}
                         {:id "hello"
                          :source-paths ["node-cljs/hello"]
                          :externs []
                          :compiler {
                                      :target :nodejs
                                      :output-to "target/nodejs/hello-node.js"
                                      :optimizations :simple
                                      :pretty-print true}}
                         {:id "phantom"
                          :source-paths ["node-cljs/phantom"]
                          :externs []
                          :compiler {
                                      :target :nodejs
                                      :output-to "target/nodejs/ph-node.js"
                                      :optimizations :whitespace
                                      :pretty-print true}}
                         ]}
  :repositories {"clojars.org" "http://clojars.org/repo"
                 "java.net" "http://download.java.net/maven/2"
                 "sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"})
