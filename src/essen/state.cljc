(ns essen.state
  (:refer-clojure :exclude [atom])
  (:require
   #?(:cljs [reagent.core :as r])
   [integrant-tools.core :as it]))

(defn atom [v]
  #?(:clj  (clojure.core/atom v)
     :cljs (r/atom v)))

(defonce state (atom {}))

(defonce systems (atom {}))

(defonce game (atom {}))

(defonce messages (atom {}))

(defonce input-messages (atom {}))

(defonce persistent-entities (atom {}))

(defn get-scene [scene-key]
  (get-in @state [:essen/scenes scene-key]))

(defn reset-events! [scene-key]
  (swap! messages assoc scene-key (atom []))
  (swap! input-messages assoc scene-key (atom [])))

(defn save-state! [scene-key]
  (swap! state assoc-in [:essen/scenes scene-key]
         (-> (get-in @systems [:essen/scenes scene-key])
             (it/find-derived-value scene-key)
             (atom))))

(defn save-system! [system scene-key]
  (swap! systems assoc-in [:essen/scenes scene-key] system))
