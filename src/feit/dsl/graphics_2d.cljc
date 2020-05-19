(ns feit.dsl.graphics-2d
  (:require [integrant.core :as ig]
            [meta-merge.core :refer [meta-merge]]
            [feit.dsl :as r]))

(defn- merge-component [config m]
  (update-in config (:component/last (meta config)) merge m))

(defn- meta-merge-component [config m]
  (update-in config (:component/last (meta config)) meta-merge m))

(defn center-canvas
  "TODO"
  [config]
  (meta-merge config
              {:feit.interface.graphics-2d/system
               {:graphics-2d.window/center-canvas? true}}))

(defn set-dimensions
  "TODO"
  [config width height]
  (meta-merge config
              {:feit.interface.graphics-2d/system
               {:graphics-2d.window/width width
                :graphics-2d.window/height height}}))

(defn auto-scale
  "TODO"
  [config]
  (meta-merge config
              {:feit.interface.graphics-2d/system
               {:graphics-2d.window/auto-scale true}}))

(defn on-resize
  "TODO"
  [config k]
  (meta-merge config
              {:feit.interface.graphics-2d/system
               {:graphics-2d.window/on-resize (ig/ref k)}
               k {}}))

(defn component-animated-sprite
  "TODO"
  ([config k spritesheet-name spritesheet-animation]
   (component-animated-sprite config k spritesheet-name spritesheet-animation {}))
  ([config k spritesheet-name spritesheet-animation opts]
   (r/ref-component config [:graphics-2d.component/sprite k]
                    (merge {:spritesheet/animation spritesheet-animation
                            :spritesheet/name spritesheet-name}
                           opts))))

(defn component-animated-sprite+ref
  "TODO"
  ([config k spritesheet-name spritesheet-animation]
   (component-animated-sprite+ref config k spritesheet-name spritesheet-animation {}))
  ([config k spritesheet-name spritesheet-animation opts]
   (-> config
       (component-animated-sprite k spritesheet-name spritesheet-animation opts)
       (r/ref-component k))))

(defn component-sprite
  "TODO"
  ([config k]
   (component-sprite config k {}))
  ([config k opts]
   (r/component config [:graphics-2d.component/sprite k] opts)))

(defn component-sprite+ref
  "TODO"
  ([config k]
   (component-sprite+ref config k {}))
  ([config k opts]
   (-> config
       (component-sprite k opts)
       (r/ref-component k))))

(defn component-loader
  "TODO"
  ([config k]
   (component-loader config k {}))
  ([config k opts]
   (r/component config [:graphics-2d.component/loader k] opts)))

(defn component-loader+ref
  "TODO"
  ([config k]
   (component-loader+ref config k {}))
  ([config k opts]
   (-> config
       (component-loader k opts)
       (r/ref-component [:graphics-2d.component/loader k]))))

(defn component-rectangle
  "TODO"
  ([config k x y w h fill]
   (component-rectangle config k x y w h fill {}))
  ([config k x y w h fill opts]
   (r/component config [:graphics-2d.component/rectangle k]
                (merge {:position/x x :position/y y :shape/w w :shape/h h :shape/fill fill}
                       opts))))

(defn component-rectangle+ref
  "TODO"
  ([config k x y w h fill]
   (component-rectangle+ref config k x y w h fill {}))
  ([config k x y w h fill opts]
   (-> config
       (component-rectangle k x y w h fill opts)
       (r/ref-component k))))

(defn loader-next-scene
  "TODO"
  [config next-scene]
  (merge-component config {:loader/next-scene next-scene}))

(defn loader-spritesheet
  "TODO"
  [config spritesheet-file spritesheet-name]
  (meta-merge-component config {:loader/spritesheets
                                [{:spritesheet/name spritesheet-name
                                  :spritesheet/file spritesheet-file}]}))

(defn loader-texture
  "TODO"
  ([config texture-file]
   (loader-texture config texture-file texture-file))
  ([config texture-file texture-name]
   (meta-merge-component config {:loader/textures
                                 [{:texture/file texture-file
                                   :texture/name texture-name}]})))

(defn sprite-spritesheet-animation
  "TODO"
  [config spritesheet-name spritesheet-animation]
  (merge-component config {:spritesheet/name spritesheet-name
                           :spritesheet/animation spritesheet-animation}))

(defn sprite-spritesheet-texture
  "TODO"
  [config spritesheet-name spritesheet-texture]
  (merge-component config {:spritesheet/name spritesheet-name
                           :spritesheet/texture spritesheet-texture}))

(defn sprite-texture
  "TODO"
  [config texture-name]
  (merge-component config {:texture/name texture-name}))

(defn sprite-position
  "TODO"
  [config position-x position-y]
  (merge-component config {:position/x position-x
                           :position/y position-y}))

(defn sprite-flip
  "TODO"
  [config flip-x flip-y]
  (merge-component config {:flip/x flip-x
                           :flip/y flip-y}))

(defn sprite-on-click
  "TODO"
  [config handler]
  (meta-merge-component config {:handler/on-click [handler]}))

(defn sprite-scale
  "TODO"
  [config scale]
  (meta-merge-component config {:sprite/scale scale}))

(defn sprite-fps
  "TODO"
  [config fps]
  (meta-merge-component config {:sprite/fps fps}))
