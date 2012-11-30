
(defproject oasys_corp "1.0.0-SNAPSHOT"
  :description "Building the Ultimate Web Framework!!!!"
  :dependencies [[org.clojure/clojure "1.5.0-alpha5"]
                 ;[org.clojure/clojure "1.5.0-master-SNAPSHOT"]
                 ;[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [clj-time "0.4.3"]
                 [org.clojure/data.json "0.1.1"]
                 [cheshire "5.0.0"]
                 [congomongo/congomongo "0.2.0"]
                 ;[congomongo/congomongo "0.3.3"]
                 [org.clojars.hozumi/mongodb-session "1.0.1"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [korma "0.3.0-beta11"]
                 [hiccup/hiccup "1.0.1"]
                 [clj-style/clj-style "1.0.1"]
                 [ring/ring-core "1.1.5"]
                 [ring/ring "1.1.5" :exclusions [ring/ring-jetty-adapter]]
                 [compojure/compojure "1.1.3"]
                 [com.novemberain/validateur "1.1.0"]
                 [me.shenfeng/async-ring-adapter "1.0.1" :exclusions [[org.jboss.netty/netty]
                                                                      [io.netty/netty]]]
;                 [org.jboss.netty/netty "3.2.7.Final"]
                 [io.netty/netty "3.5.7.Final"]
                 [clojurewerkz/quartzite "1.0.0-rc6"]
                 [com.novemberain/quartz-mongodb "1.0.0"]
                 [com.draines/postal "1.8.0"]
                 [clj-http "0.5.6"]
                 [clj-oauth2 "0.2.0"]
                 [cheshire "4.0.1"]
                 [commons-fileupload/commons-fileupload "1.2.2"]
                 [rome/rome "1.0"]
                 [org.apache.tika/tika-core "1.0"]
                 [org.apache.tika/tika-parsers "1.0"]
;                 [org.apache.solr/solr-core "3.6.1"]
;                 [org.apache.solr/solr-solrj "3.6.1"]
;                 [org.apache.solr/solr-clustering "3.6.1"]
                 
;http://code.google.com/p/flying-saucer/
                 
                 [org.xhtmlrenderer/flying-saucer-core "9.0.0"]
                 [org.xhtmlrenderer/flying-saucer-pdf "9.0.0"]
                 [org.xhtmlrenderer/flying-saucer-log4j "9.0.0"]
;                 [clj-pdf "0.9.1"]
                 
                 [org.glassfish/javax.servlet "3.1.1"]
                 [org.eclipse.jetty/jetty-server "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-security "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-servlet "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-webapp "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-servlets "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-xml "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-util "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-jmx "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-http "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-io "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-continuation "8.1.4.v20120524"]
	               [org.eclipse.jetty/jetty-websocket "8.1.4.v20120524"]
                 ;;;;;;;;;;;;;;;  LOGGING    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
                 [org.slf4j/slf4j-api "1.6.6"]
                 [org.slf4j/jcl-over-slf4j "1.6.6"]
                 [ch.qos.logback/logback-core "1.0.6"]
                 [ch.qos.logback/logback-classic "1.0.6"]
                 [ch.qos.logback/logback-access "1.0.6"]
                 [ch.qos.logback/logback-site "1.0.6"]
                 ;;;;;;;;;;;;;;;;; QUEUE  ;;;;;;;;;;;;;;;;;;
                 ;[resque-clojure "0.2.2"]
                 ;;;;;;;;;;;;;;;;; ZEROMQ ;;;;;;;;;;;;;;;;;;
                 [org.zeromq/zeromq-scala-binding_2.9.1 "0.0.6"]
                 ;;;;;;;;;;; CLJS LIBS ;;;;;;;;;;;;;;;;;;;;;;;;
;                 [jayq "0.1.0-alpha4"]
;                 [waltz "0.1.0-alpha1"]
;                 [fetch "0.1.0-alpha2"]
;                 [crate "0.2.0-alpha3"]
;                 [monet "0.1.0-SNAPSHOT"]
;                 [enfocus "0.9.1-SNAPSHOT"]
;                 [com.keminglabs/c2 "0.1.2"]
                 ]
  :plugins [[lein-swank "1.4.4"]
            [lein-cljsbuild "0.2.8"]
            [lein-light "0.0.13"]
            [lein-depgraph "0.1.0"]
            [lein-cucumber "1.0.1"]
            [lein-midje "2.0.1"]
            ;[com.stuartsierra/lazytest "1.2.3"]
            ;[clj-ns-browser "1.2.0"]
            [lein-ring "0.7.5"]
            ;:plugins [[lein-git-deps "0.0.1-SNAPSHOT"]]
            [lein-scalac "0.1.0"]
            [lein-marginalia "0.7.1"]
            [lein-catnip "0.5.0"]]
  :prep-tasks ["scalac" "javac" ["cljsbuild" "once"]]
  :ring {:handler com.outcastgeek.services.web.Services/website}
  :aot :all
;  :jvm-opts ["-Xmx1g" "-server"
;             "-Dhttp.proxyHost=webproxy.int.westgroup.com"
;             "-Dhttp.proxyPort=80"
;             "-Dhttps.proxyHost=webproxy.int.westgroup.com"
;             "-Dhttps.proxyPort=80"]
  :jvm-opts ["-Xmx1g" "-server" "-Xms1g" "-XX:MaxPermSize=128m"
             "-XX:CompileThreshold=4" "-XX:+AggressiveOpts"
             "-XX:MaxHeapFreeRatio=70" "-XX:MinHeapFreeRatio=40"
             "-XX:+UseParallelGC" "-XX:ParallelGCThreads=24"
             "-XX:+DisableExplicitGC" "-Djava.awt.headless=true"]
  ;; Options to pass to java compiler for java source,
  ;; exactly the same as command line arguments to javac
;  :javac-options {:target "1.7"
;                  :debug "off"
;                  :source "1.7"}
  :javac-options ["-target" "1.7" "-source" "1.7" "-Xlint:-options"]
  ;; Leave the contents of :source-paths out of jars (for AOT projects)
  :omit-source true
  :main com.outcastgeek.services.web.Services
  :scala-source-path "scala"
  :java-source-paths ["java"]
;  :java-source-path "java"
  :source-path "src"
  :cljsbuild {
    :builds [
;             {
;			        ; The path to the top-level ClojureScript source directory:
;			        :source-path "src-cljs/example"
;			        ; The standard ClojureScript compiler options:
;			        ; (See the ClojureScript compiler documentation for details.)
;			        :compiler {
;			          :output-to "resources/public/static/javascript/example/example.js"  ; default: main.js in current directory
;			          :optimizations :advanced
;			          :pretty-print false}}
             {
               ; The path to the top-level ClojureScript source directory:
               :source-path "src-cljs/outcastgeek"
               ; The standard ClojureScript compiler options:
               ; (See the ClojureScript compiler documentation for details.)
               :compiler {
                           :output-to "resources/public/static/javascript/outcastgeek/outcastgeek.js"  ; default: main.js in current directory
                           :optimizations :simple
                           :libs ["resources/public/static/javascript/outcastgeek/closure"]
                           :pretty-print true}}
;             {
;               ; The path to the top-level ClojureScript source directory:
;               :source-path "src-cljs/jayq"
;               ; The standard ClojureScript compiler options:
;               ; (See the ClojureScript compiler documentation for details.)
;               :compiler {
;                           :output-to "resources/public/static/javascript/jayq/jayq.js"  ; default: main.js in current directory
;                           :optimizations :whitespace
;                           :externs ["resources/public/static/javascript/jquery/jquery.js"]
;                           :pretty-print true}}
;             {
;               ; The path to the top-level ClojureScript source directory:
;               :source-path "src-cljs/crate"
;               ; The standard ClojureScript compiler options:
;               ; (See the ClojureScript compiler documentation for details.)
;               :compiler {
;                           :output-to "resources/public/static/javascript/crate/crate.js"  ; default: main.js in current directory
;                           :optimizations :whitespace
;                           :pretty-print true}}
;             {
;               ; The path to the top-level ClojureScript source directory:
;               :source-path "src-cljs/c2"
;               ; The standard ClojureScript compiler options:
;               ; (See the ClojureScript compiler documentation for details.)
;               :compiler {
;                           :output-to "resources/public/static/javascript/c2/c2.js"  ; default: main.js in current directory
;                           :optimizations :whitespace
;                           :pretty-print true}}
             ]}
  ;; You can also set the policies for how to handle :checksum
  ;; failures to :fail, :warn, or :ignore. In :releases, :daily,
  ;; :always, and :never are supported.
  :checksum :warn
  :update :daily
  :repositories {"java.net" "http://download.java.net/maven/2"
                 ;"sonatype.snapshots" "https://oss.sonatype.org/content/repositories/snapshots"
                 ;"akka.io" "http://repo.akka.io/releases"
                 ;"akka.io.snapshots" "http://repo.akka.io/snapshots"
                 ;"spring.milestone" "http://maven.springframework.org/milestone"
                 ;"stuart" "http://stuartsierra.com/maven2"
                 })
