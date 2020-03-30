(ns essen.state
  (:refer-clojure :exclude [atom])
  (:require
   #?(:cljs [reagent.core :as r])))

(defn atom [v]
  #?(:clj  (clojure.core/atom v)
     :cljs (r/atom v)))

(defonce state (atom {}))

(defonce system (atom {}))

(defonce config (atom {}))

(defonce events (atom {}))

(defonce input-events (atom {}))

(defonce persistent-entities (atom {}))

(defonce entities (atom {}))

(defonce persistent-components (atom {}))

(defn get-scene [scene-key]
  (get-in @state [:essen/scenes scene-key]))

(defn reset-events! [scene-key]
  (swap! events assoc scene-key (atom []))
  (swap! input-events assoc scene-key (atom [])))

(defn save-scene! [scene]
  (swap! state assoc-in [:essen/scenes (:scene/key scene)] (atom scene)))

(defn reset-state! [scene-key]
  (swap! state update :essen/scenes dissoc scene-key))
