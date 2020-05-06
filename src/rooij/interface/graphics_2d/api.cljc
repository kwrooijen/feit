(ns rooij.interface.graphics-2d.api
  (:require [integrant.core :as ig]
            [meta-merge.core :refer [meta-merge]]
            [rooij.dsl :as r]))

(defn center-canvas [config]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/center-canvas? true}}))

(defn set-dimensions [config width height]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/width width
                :graphics-2d.window/height height}}))

(defn auto-scale [config]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/auto-scale true}}))

(defn on-resize [config k]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/on-resize (ig/ref k)}
               k {}}))

(defn- merge-component [config m]
  (update-in config (:component/last (meta config)) merge m))

(defn- meta-merge-component [config m]
  (update-in config (:component/last (meta config)) merge m))

(defn component-animated-sprite
  ([config k spritesheet-name spritesheet-animation]
   (component-animated-sprite config k spritesheet-name spritesheet-animation {}))
  ([config k spritesheet-name spritesheet-animation opts]
   (r/ref-component config [:graphics-2d.component/sprite k]
                    (merge {:spritesheet/animation spritesheet-animation
                            :spritesheet/name spritesheet-name}
                           opts))))

(defn sprite-position [config position-x position-y]
  (merge-component config {:position/x position-x
                           :position/y position-y}))

(defn sprite-flip [config flip-x flip-y]
  (merge-component config {:flip/x flip-x
                           :flip/y flip-y}))

(defn sprite-on-click [config handler]
  (meta-merge-component config {:handler/on-click [handler]}))
