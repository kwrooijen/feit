(ns rooij.module.box2d.state
  (:require
   ["planck-js" :as planck]))

(def ^:private worlds (atom {}))

(defonce ^:private running-worlds (atom #{}))

(defn get-world [scene-key]
  (get @worlds scene-key))

(defn init-world! [scene-key]
  (swap! worlds assoc scene-key (.World planck))
  (swap! running-worlds conj scene-key))

(defn halt-world! [scene-key]
  ;; TODO destroy object
  (swap! worlds dissoc scene-key)
  (swap! running-worlds disj scene-key))
