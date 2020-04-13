(ns rooij.state
  #?(:cljs
     (:refer-clojure :exclude [atom]))
  #?(:cljs
     (:require
      [reagent.core :as r])))

#?(:cljs
   (defn atom [v]
     (r/atom v)))

(defonce system (atom {}))

;; TODO Maybe create a protocol?
(defonce ^:dynamic graphics-2d (fn [_scene-key]))

(defonce ^:dynamic graphics-2d-scene {:init identity :halt! identity})

(defonce ^:dynamic physics-2d (fn [_delta _scene-key]))

(defonce ^:dynamic physics-2d-scene {:init identity :halt! identity})

(defonce ^:private scenes (atom {}))

(defonce ^:private events (atom {}))

(defonce ^:private input-events (atom {}))

(defonce ^:private persistent-components (atom {}))

(defn reset-events! [scene-key]
  (swap! events assoc scene-key (atom []))
  (swap! input-events assoc scene-key (atom [])))

(defn get-scenes []
  @scenes)

(defn get-scene [scene-key]
  (get @scenes scene-key))

(defn get-scene-events [scene-key]
  (get @events scene-key))

(defn save-scene! [scene]
  (swap! scenes assoc (:scene/key scene) (atom scene)))

(defn remove-scene! [scene-key]
  (swap! scenes dissoc scene-key))

(defn get-component [entity-key component-key]
  (get @persistent-components [entity-key component-key]))

(defn save-component! [state entity-key component-key]
  (swap! persistent-components assoc [entity-key component-key] state))

(defn get-input-events
  ([] @input-events)
  ([scene-key] (get @input-events scene-key)))

(defn set-graphics-2d! [v]
  (set! graphics-2d v))

(defn set-graphics-2d-scene! [v]
  (set! graphics-2d-scene v))

(defn set-physics-2d! [v]
  (set! physics-2d v))

(defn set-physics-2d-scene! [v]
  (set! physics-2d-scene v))
