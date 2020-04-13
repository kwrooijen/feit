(ns rooij.config
  (:require
   [meta-merge.core :refer [meta-merge]]))

(defonce config (atom {}))

(defonce default-config
  {:rooij.interface.physics-2d/system {}
   :rooij.interface.physics-2d/scene {}
   :rooij.interface.graphics-2d/system {}
   :rooij.interface.graphics-2d/scene {}})

(defonce extension-config
  (atom {}))

(defonce user-config
  (atom {}))

(defn reset-config!
  ([] (reset-config! (meta-merge default-config @extension-config @user-config)))
  ([c] (reset! config c)))

(defn reset-user-config! []
  (reset! user-config {}))

(defn merge-extension! [config]
  (swap! extension-config meta-merge config)
  (reset-config!))

(defn merge-user! [config]
  (swap! user-config meta-merge config)
  (reset-config!))
