(ns feit.config
  (:require
   [meta-merge.core :refer [meta-merge]]))

(defonce config (atom {}))

(defonce interface-config
  (atom {}))

(defonce extension-config
  (atom {}))

(defonce user-config
  (atom {}))

(defn reset-config!
  ([] (reset-config! (meta-merge @interface-config @extension-config @user-config)))
  ([c] (reset! config c)))

(defn reset-user-config! []
  (reset! user-config {}))

(defn merge-interface! [config]
  (swap! interface-config meta-merge config)
  (reset-config!))

(defn merge-extension! [config]
  (swap! extension-config meta-merge config)
  (reset-config!))

(defn merge-user! [config]
  (swap! user-config meta-merge config)
  (reset-config!))
