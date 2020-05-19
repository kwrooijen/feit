(ns feit.module.pixi.interface
  (:require
   [feit.interface.graphics-2d.core :refer [FeitGraphics2D]]
   [feit.module.pixi.debug :as pixi.debug]
   [feit.module.pixi.interface.loader :as interface.loader]
   [feit.module.pixi.interface.rectangle :as interface.rectangle]
   [feit.module.pixi.interface.sprite :as interface.sprite]
   [feit.module.pixi.screen :as pixi.screen]
   [feit.module.pixi.state :as state]))

(deftype PixiGraphics2D [init-opts]
  FeitGraphics2D
  (scene-init [this scene-key]
    (state/init-scene! scene-key)
    (pixi.screen/update-scene-scale))

  (scene-halt! [this scene-key]
    (state/halt-scene! scene-key) )

  (step [this scene-key]
    (when-let [scene (state/get-scene scene-key)]
      (.render state/renderer scene)))

  (draw-wireframe [this scene-key vectors]
    (pixi.debug/draw-wireframe scene-key vectors))

  (make-sprite [this opts]
    (interface.sprite/make opts))

  (make-rectangle [this opts]
    (interface.rectangle/make opts))

  (make-loader [this opts]
    (interface.loader/make opts)))
