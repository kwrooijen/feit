(ns rooij.interface.graphics-2d.dsl
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
  (update-in config (:component/last (meta config)) meta-merge m))

(defn component-animated-sprite
  ([config k spritesheet-name spritesheet-animation]
   (component-animated-sprite config k spritesheet-name spritesheet-animation {}))
  ([config k spritesheet-name spritesheet-animation opts]
   (r/ref-component config [:graphics-2d.component/sprite k]
                    (merge {:spritesheet/animation spritesheet-animation
                            :spritesheet/name spritesheet-name}
                           opts))))

(defn component-sprite
  ([config k]
   (component-sprite config k {}))
  ([config k opts]
   (r/ref-component config [:graphics-2d.component/sprite k] opts)))

(defn component-loader
  ([config k]
   (component-loader config k {}))
  ([config k opts]
   (r/ref-component config [:graphics-2d.component/loader k] opts)))

(defn loader-next-scene [config next-scene]
  (merge-component config {:loader/next-scene next-scene}))

(defn loader-spritesheet [config spritesheet-file spritesheet-name]
  (meta-merge-component config {:loader/spritesheets [{:spritesheet/name spritesheet-name
                                                       :spritesheet/file spritesheet-file}]}))

(defn loader-texture
  ([config texture-file]
   (loader-texture config texture-file texture-file))
  ([config texture-file texture-name]
   (meta-merge-component config {:loader/textures
                                 [{:texture/file texture-file
                                   :texture/name texture-name}]})))

(defn sprite-spritesheet-animation [config spritesheet-name spritesheet-animation]
  (merge-component config {:spritesheet/name spritesheet-name
                           :spritesheet/animation spritesheet-animation}))

(defn sprite-spritesheet-texture [config spritesheet-name spritesheet-texture]
  (merge-component config {:spritesheet/name spritesheet-name
                           :spritesheet/texture spritesheet-texture}))

(defn sprite-texture [config texture-name]
  (merge-component config {:texture/name texture-name}))

(defn sprite-position [config position-x position-y]
  (merge-component config {:position/x position-x
                           :position/y position-y}))

(defn sprite-flip [config flip-x flip-y]
  (merge-component config {:flip/x flip-x
                           :flip/y flip-y}))

(defn sprite-on-click [config handler]
  (meta-merge-component config {:handler/on-click [handler]}))
