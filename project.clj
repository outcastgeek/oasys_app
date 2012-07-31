
(defproject oasys_corp "1.0.0-SNAPSHOT"
  :description "Building the Ultimate Web Framework!!!!"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [redis.clients/jedis "2.0.0"]
                 [resque-clojure "0.2.2"]
                 [congomongo/congomongo "0.1.9"]
                 [org.clojars.hozumi/mongodb-session "1.0.1"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [korma "0.3.0-beta11"]
                 [hiccup/hiccup "1.0.0"]
                 [clj-style/clj-style "1.0.1"]
                 [ring/ring-core "1.1.1"]
                 [ring/ring "1.1.1" :exclusions [ring/ring-jetty-adapter]]
                 [compojure/compojure "1.1.1"]
                 [me.shenfeng/async-ring-adapter "1.0.1"]
                 [clj-http "0.5.0"]
                 [clj-oauth2 "0.2.0"]
                 [cheshire "4.0.1"]
                 [commons-fileupload/commons-fileupload "1.2.2"]
                 [rome/rome "1.0"]
                 [org.apache.tika/tika-core "1.0"]
                 [org.apache.tika/tika-parsers "1.0"]
                 
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
                 
                 [org.slf4j/slf4j-api "1.6.6"]
                 [org.slf4j/jcl-over-slf4j "1.6.6"]
                 [ch.qos.logback/logback-core "1.0.6"]
                 [ch.qos.logback/logback-classic "1.0.6"]
                 [ch.qos.logback/logback-access "1.0.6"]
                 [ch.qos.logback/logback-site "1.0.6"]
                 ;;;;;;;;;;;;;; AKKA ;;;;;;;;;;;;;;;;;;;;;;;;;;
;                 [com.typesafe.akka/akka-actor "2.0.2"]
;                 [com.typesafe.akka/akka-remote "2.0.2"]
;                 [com.typesafe.akka/akka-file-mailbox "2.0.2"]
;                 [com.typesafe.akka/akka-slf4j "2.0.2"]
                 ;;;;;;;;;;; SPRING ;;;;;;;;;;;;;;;;;;;;;;;;
                 [org.springframework/spring-context "3.1.2.RELEASE"]
                 [org.springframework/spring-web "3.1.2.RELEASE"]
                 [com.outcastgeek/OasysData "0.1.0.BUILD-SNAPSHOT"]
                 ;;;;;;;;;;; CLJS LIBS ;;;;;;;;;;;;;;;;;;;;;;;;
;                 [jayq "0.1.0-alpha4"]
;                 [waltz "0.1.0-alpha1"]
;                 [fetch "0.1.0-alpha2"]
;                 [crate "0.2.0-alpha3"]
;                 [monet "0.1.0-SNAPSHOT"]
;                 [enfocus "0.9.1-SNAPSHOT"]
;                 [com.keminglabs/c2 "0.1.2"]
                 ]
  :dev-dependencies [[lein-swank "1.4.4"]
                     [lein-cljsbuild "0.2.4"]]
  :aot :all
;  :jvm-opts ["-Xmx1g" "-server"
;             "-Dhttp.proxyHost=webproxy.int.westgroup.com"
;             "-Dhttp.proxyPort=80"
;             "-Dhttps.proxyHost=webproxy.int.westgroup.com"
;             "-Dhttps.proxyPort=80"]
  :jvm-opts ["-Xmx1g" "-server"]
  :main com.outcastgeek.services.web.Services
  :java-source-path "java"
  :source-path "src"
  :hooks [leiningen.cljsbuild]
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
  :repositories {"akka.io" "http://repo.akka.io/releases"
                 "java.net" "http://download.java.net/maven/2"
                 "spring.milestone" "http://maven.springframework.org/milestone"})
