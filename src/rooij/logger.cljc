(ns rooij.logger
  (:require
   [taoensso.timbre.appenders.core :as appenders]
   [taoensso.timbre :as timbre]
   #?(:clj [clojure.pprint :refer [pprint]]
      :cljs [cljs.pprint :refer [pprint]])))

(defn format-pprint [v]
  (if (string? v) v (with-out-str (pprint v))))

(defn pprint-middleware [data]
  (update data :vargs (partial mapv format-pprint)))

(defn timbre-config [level]
  {:level level
   :ns-whitelist  []
   :ns-blacklist  []
   :middleware [pprint-middleware]
   :appenders
   {:println-appender
    #?(:clj  (appenders/println-appender)
       :cljs (appenders/console-appender))}})

(defn disable! []
  (timbre/set-config! {}))

(defn enable!
  ([] (enable! :error))
  ([level]
   (timbre/set-config! (timbre-config level))))
