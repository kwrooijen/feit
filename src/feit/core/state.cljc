(ns feit.core.state
  #?(:cljs
     (:refer-clojure :exclude [atom]))
  #?(:cljs
     (:require
      [reagent.core :as r])))

#?(:cljs
   (defn atom [v]
     (r/atom v)))

(defonce system (atom {}))

(defonce ^:dynamic graphics-2d nil)

(defonce ^:dynamic physics-2d nil)

(defonce ^:dynamic keyboard nil)

;; TODO Maybe make scenes volatile!, that might be a critital performance boost
;; since we update the scene atom after every message.
(defonce ^:private scenes (atom {}))

(defonce ^:private events (atom {}))

(defonce ^:private post-events (atom {}))

(defonce ^:private input-events (atom {}))

(defonce ^:private down-keys (atom #{}))

(defonce ^:private persistent-components (atom {}))

(defonce wireframe-enabled? (atom false))

(defn reset-events! [scene-key]
  (swap! events assoc scene-key (atom []))
  (swap! post-events assoc scene-key (atom []))
  (swap! input-events assoc scene-key (atom [])))

(defn get-scenes []
  @scenes)

(defn- scene-does-not-exist-error [scene-key]
  (throw (ex-info (str "Scene " (pr-str scene-key) " does not exist, or is not yet initiated")
                  {::scene-key scene-key})))

(defn get-scene [scene-key]
  (if-let [scene (get @scenes scene-key)]
    scene
    (scene-does-not-exist-error scene-key)))

(defn get-scene-events [scene-key]
  (if-let [scene-events (get @events scene-key)]
    scene-events
    (scene-does-not-exist-error scene-key)))

(defn get-scene-post-events [scene-key]
  (if-let [post-events (get @post-events scene-key)]
    post-events
    (scene-does-not-exist-error scene-key)))

(defn save-scene! [scene]
  (swap! scenes assoc (:scene/key scene) (volatile! scene)))

(defn remove-scene! [scene-key]
  (swap! scenes dissoc scene-key))

(defn get-component [entity-key component-key]
  (get @persistent-components [entity-key component-key]))

(defn save-component! [state entity-key component-key]
  (swap! persistent-components assoc [entity-key component-key] state))

(defn get-input-events
  ([] @input-events)
  ([scene-key] (get @input-events scene-key)))

(defn get-down-keys []
  @down-keys)

(defn add-down-key! [key]
  (swap! down-keys conj key))

(defn remove-down-key! [key]
  (swap! down-keys disj key))

(defn set-graphics-2d! [v]
  (set! graphics-2d v))

(defn set-physics-2d! [v]
  (set! physics-2d v))

(defn set-keyboard! [v]
  (set! keyboard v))

(comment
  (tap> @(feit.state/get-scene :scene/battle))
  (tap> @(feit.state/get-scene :scene/organize))
  (tap> @(feit.state/get-scene :scene/loading)))
