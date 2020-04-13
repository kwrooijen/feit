(ns rooij.module.matterjs.state
  (:require
   ["matter-js" :as Matter :refer [Engine World Mouse MouseConstraint]]))

(defonce ^:private engines (atom nil))

(defonce ^:private running-engines (atom #{}))

(defn get-engine [scene-key]
  (get @engines scene-key))

(defn init-engine! [scene-key]
  (swap! engines assoc scene-key (.create Engine))
  (swap! running-engines conj scene-key))

(defn halt-engine! [scene-key]
  (.clear Engine (get-engine scene-key))
  (swap! engines dissoc scene-key)
  (swap! running-engines disj scene-key))
