
(defproject oasys_corp "1.0.0-SNAPSHOT"
  :description "Building the Ultimate Web Framework!!!!"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [rrss/rrss "0.2.2"]
                 [resque-clojure "0.2.2"]
                 [congomongo/congomongo "0.1.9"]
                 [org.clojars.hozumi/mongodb-session "1.0.1"]
                 [mysql/mysql-connector-java "5.1.20"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [clj-record "1.1.1"]

;Check this out: https://www.x.com/developers/x.commerce/documentation-tools/api/profile-service-api-reference

;(require '[clojure.java.jdbc :as sql])
;
;(def db {:classname "com.postgresql.Driver"
;        :subprotocol "postgresql"
;        :user "postgres"
;        :password "PgSQL2012!"
;        :subname "//localhost/postgres"})
;
;(def db {:classname "com.mysql.jdbc.Driver"
;        :subprotocol "mysql"
;        :user "root"
;        :password "MySQL2012"
;        :subname "//localhost/lambert"})
;
;(defn create-database [name]
; (sql/with-connection db
;   (with-open [s (.createStatement (sql/connection))]
;     (.addBatch s (str "create database" name))
;     (seq (.executeBatch s)))))
;
;(defn drop-database [name]
; (sql/with-connection db
;   (with-open [s (.createStatement (sql/connection))]
;     (.addBatch s (str "drop database " name))
;     (seq (.executeBatch s)))))
;
;(defn create-tables
; "Creates the table needed to store the
;winners and nominees."
; []
; (sql/create-table
;   :nominees
;   [:id :integer "PRIMARY KEY"]
;   [:year :integer]
;   [:title "varchar(64)"]
;   [:author "varchar(32)"]
;   [:winner "tinyint"]
;   [:read_it "tinyint"]
;   [:own_it "tinyint"]
;   [:want_it "tinyint"]))
;
;(defn create-db-tables
; "Creates a new database and tables"
; []
; (sql/with-connection db
;   (create-tables)))
;
;(create-database "lambert")
;
;(drop-database "lambert")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(require '[clojure.java.jdbc :as sql])
;
;(def db {:classname "com.mysql.jdbc.Driver"
;                          :subprotocol "mysql"
;                          :user "root"
;                          :password "MySQL2012"
;                          :subname "//localhost/lambert"})

;(require '[clojure.java.jdbc :as sql])
;
;(def db {:classname "com.postgresql.Driver"
;                          :subprotocol "postgresql"
;                          :user "postgres"
;                          :password "PgSQL2012!"
;                          :subname "//localhost/lambert"})
                 
;(defn create-database [name]
;  (sql/with-connection db
;    (with-open [s (.createStatement (sql/connection))]
;      (.addBatch s (str "create database " name))
;      (seq (.executeBatch s)))))
;
;(defn drop-database [name]
;  (sql/with-connection db
;    (with-open [s (.createStatement (sql/connection))]
;      (.addBatch s (str "drop database " name))
;      (seq (.executeBatch s)))))

;                 (ns com.example.city
;                   (:require clj-record.boot))
;
;
;                 (def db {:classname "com.mysql.jdbc.Driver"
;                          :subprotocol "mysql"
;                          :user "root"
;                          :password "MySQL2012"
;                          :subname "//localhost/world"})
;
;                 (clj-record.core/init-model
;                   :table-name "city")
;
;                 (def father-rec (com.example.city/create {:Name "Abidjan"
;                                                           :CountryCode 225
;                                                           :District "Cocody"
;                                                           :Population 5000000}))





                 [hiccup/hiccup "1.0.0"]
                 [clj-style/clj-style "1.0.1"]
                 [ring/ring-core "1.1.1"]
                 [ring/ring "1.1.1"]
                 [clj-http "0.4.3"]
                 [clj-oauth2 "0.2.0"]
                 [compojure/compojure "1.1.0"]
                 [com.cemerick/friend "0.0.9"]
                 [cheshire "4.0.0"]
                 [com.google.code.gson/gson "2.1"]
                 [com.thoughtworks.xstream/xstream "1.4.2"]
                 [commons-fileupload/commons-fileupload "1.2.2"]
                 [rome/rome "1.0"]
                 [org.apache.tika/tika-core "1.0"]
                 [org.apache.tika/tika-parsers "1.0"]
                 [com.lowagie/itext "2.0.8"]
                 [org.xhtmlrenderer/core-renderer "R8"]
                 [clj-pdf "0.9.1"]
                 [org.glassfish/javax.servlet "3.1.1"]
;                 [org.slf4j/slf4j-log4j12 "1.6.6"]
                 [org.slf4j/slf4j-api "1.6.6"]
                 [org.slf4j/jcl-over-slf4j "1.6.6"]
                 [ch.qos.logback/logback-core "1.0.6"]
                 [ch.qos.logback/logback-classic "1.0.6"]
                 [ch.qos.logback/logback-access "1.0.6"]
                 [ch.qos.logback/logback-site "1.0.6"]
                 [org.jboss.netty/netty "3.2.7.Final"]
                 ;;;;;;;;;;;;;; AKKA ;;;;;;;;;;;;;;;;;;;;;;;;;;
                 [com.typesafe.akka/akka-actor "2.0.2"]
                 [com.typesafe.akka/akka-remote "2.0.2"]
                 [com.typesafe.akka/akka-file-mailbox "2.0.2"]
                 [com.typesafe.akka/akka-slf4j "2.0.2"]
                 ;;;;;;;;;;; CLJS LIBS ;;;;;;;;;;;;;;;;;;;;;;;;
                 [jayq "0.1.0-alpha4"]
                 [waltz "0.1.0-alpha1"]
                 [fetch "0.1.0-alpha2"]
                 [crate "0.2.0-alpha3"]
                 [monet "0.1.0-SNAPSHOT"]
                 [enfocus "0.9.1-SNAPSHOT"]
                 [com.keminglabs/c2 "0.1.2"]]
  :dev-dependencies [[lein-swank "1.4.4"]
                     [lein-cljsbuild "0.2.1"]]
  :aot :all
;  :jvm-opts ["-Xmx1g" "-server"
;             "-Dhttp.proxyHost=webproxy.int.westgroup.com"
;             "-Dhttp.proxyPort=80"
;             "-Dhttps.proxyHost=webproxy.int.westgroup.com"
;             "-Dhttps.proxyPort=80"]
  :jvm-opts ["-Xmx1g" "-server"]
  :main com.outcastgeek.services.web.Services
  :source-path "src"
  :hooks [leiningen.cljsbuild]
  :cljsbuild {
    :builds [{
        ; The path to the top-level ClojureScript source directory:
        :source-path "src-cljs/example"
        ; The standard ClojureScript compiler options:
        ; (See the ClojureScript compiler documentation for details.)
        :compiler {
          :output-to "resources/public/static/javascript/example/example.js"  ; default: main.js in current directory
          :optimizations :advanced
          :pretty-print false}}
             {
               ; The path to the top-level ClojureScript source directory:
               :source-path "src-cljs/outcastgeek"
               ; The standard ClojureScript compiler options:
               ; (See the ClojureScript compiler documentation for details.)
               :compiler {
                           :output-to "resources/public/static/javascript/outcastgeek/outcastgeek.js"  ; default: main.js in current directory
                           :optimizations :whitespace
                           :pretty-print true}}
             {
               ; The path to the top-level ClojureScript source directory:
               :source-path "src-cljs/jayq"
               ; The standard ClojureScript compiler options:
               ; (See the ClojureScript compiler documentation for details.)
               :compiler {
                           :output-to "resources/public/static/javascript/jayq/jayq.js"  ; default: main.js in current directory
                           :optimizations :whitespace
                           :externs ["resources/public/static/javascript/jquery/jquery.js"]
                           :pretty-print true}}
             {
               ; The path to the top-level ClojureScript source directory:
               :source-path "src-cljs/crate"
               ; The standard ClojureScript compiler options:
               ; (See the ClojureScript compiler documentation for details.)
               :compiler {
                           :output-to "resources/public/static/javascript/crate/crate.js"  ; default: main.js in current directory
                           :optimizations :whitespace
                           :pretty-print true}}
             {
               ; The path to the top-level ClojureScript source directory:
               :source-path "src-cljs/c2"
               ; The standard ClojureScript compiler options:
               ; (See the ClojureScript compiler documentation for details.)
               :compiler {
                           :output-to "resources/public/static/javascript/c2/c2.js"  ; default: main.js in current directory
                           :optimizations :whitespace
                           :pretty-print true}}]}
  ;; You can also set the policies for how to handle :checksum
  ;; failures to :fail, :warn, or :ignore. In :releases, :daily,
  ;; :always, and :never are supported.
  :checksum :warn
  :update :daily)
:repositories {"akka.io" "http://repo.akka.io/releases"
                  "java.net" "http://download.java.net/maven/2"}
