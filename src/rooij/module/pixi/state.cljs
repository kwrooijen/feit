(ns rooij.module.pixi.state
  (:require
   [taoensso.timbre :as timbre]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
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
  (.destroy (get-scene scene-key))
  (.clear renderer)
  (swap! scenes dissoc scene-key)
  (swap! running-scenes disj scene-key))

(defn- js-keys->clj-keys [o]
  (sp/transform [MAP-VALS] clj->js (js->clj o)))

(defn add-spritesheet! [name sheet]
  (swap! sheets assoc name sheet)
  (swap! textures assoc name (js-keys->clj-keys (.-textures sheet)))
  (swap! animations assoc name (js-keys->clj-keys (.-animations (.-spritesheet sheet)))))

(defn spritesheet-animation-texture [spritesheet animation]
  (or (get-in @animations [spritesheet animation])
      (timbre/error ::spritesheet-animation-texture-not-found
                    {:spritesheet spritesheet :animation animation})))

(defn spritesheet-static-texture [spritesheet texture]
  (or (get-in @textures [spritesheet texture])
      (timbre/error ::spritesheet-static-texture-not-found
                    {:spritesheet spritesheet :texture texture})))
