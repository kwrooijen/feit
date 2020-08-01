(defproject kwrooijen/feit "0.0.1-SNAPSHOT"
  :description "Data driven, extendable Clojure game framework"
  :url "https://github.com/kwrooijen/feit"
  :license {:name "MIT"}
  :dependencies [[reagent "0.10.0"]
                 [spec-signature "0.2.0"]
                 [integrant "0.8.0"]
                 [meta-merge "1.0.0"]
                 [integrant-tools "0.3.3"]
                 [com.rpl/specter "1.1.3"]
                 [org.clojure/core.async "1.3.610"]
                 [com.taoensso/timbre "4.10.0"]
                 [net.mikera/core.matrix "0.62.0"]]
  :plugins [[lein-cloverage "1.1.2"]
            [lein-codox "0.10.7"]
            [lein-ancient "0.6.15"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.1"]
                                  [org.clojure/clojurescript "1.10.773"]
                                  [orchestra "2020.07.12-1"]]}
             :test {:dependencies [[org.clojure/clojure "1.10.1"]
                                   [org.clojure/clojurescript "1.10.773"]
                                   [orchestra "2020.07.12-1"]]}}
  :deploy-repositories [["releases" :clojars]])
