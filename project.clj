(defproject cardo "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520"]
                 [reagent "0.8.1"] ;; FIXME: go to 0.9.0 when available to possibly fix componentWillMount warning
                 [com.google.javascript/closure-compiler-unshaded "v20190618"]
                 [org.clojure/google-closure-library "0.0-20190213-2033d5d9"]
                 [spec-signature "0.2.0"]
                 [integrant "0.8.0"]]

  :plugins []

  :min-lein-version "2.5.3"

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [thheller/shadow-cljs "2.8.39"]
                   [com.cemerick/pomegranate "1.1.0"]]}
   :prod {}})
