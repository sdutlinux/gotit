(defproject gotit "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-http "2.0.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [enlive "1.1.6"]
                 [com.taoensso/carmine "2.12.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler gotit.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
