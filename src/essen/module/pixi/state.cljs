(ns essen.module.pixi.state
  (:require
   ["pixi.js" :as PIXI :refer [Renderer Container]]))

(defonce state (atom {}))

(defonce running-scenes (atom #{}))

(defonce scenes (atom {}))

(defonce sheets (atom nil))

(defonce textures (atom nil))

(defonce animations (atom nil))

(defonce loader (.-shared PIXI/Loader))

(def ^:dynamic renderer #js {})

(defn set-renderer! [opts]
  (set! renderer (Renderer. (clj->js opts))))

(defn get-scene [scene-key]
  (get @scenes scene-key))

(defn init-scene! [scene-key]
  (swap! scenes assoc scene-key (Container.))
  (swap! running-scenes conj scene-key))

(defn halt-scene! [scene-key]
  (swap! scenes dissoc scene-key)
  (swap! running-scenes disj scene-key))
