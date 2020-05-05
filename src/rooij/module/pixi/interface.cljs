(ns rooij.module.pixi.interface
  (:require
   [rooij.interface.graphics-2d.core :refer [RooijGraphics2D]]
   [rooij.module.pixi.debug :as pixi.debug]
   [rooij.module.pixi.interface.loader :as interface.loader]
   [rooij.module.pixi.interface.rectangle :as interface.rectangle]
   [rooij.module.pixi.interface.sprite :as interface.sprite]
   [rooij.module.pixi.screen :as pixi.screen]
   [rooij.module.pixi.state :as state]))

(deftype PixiGraphics2D [init-opts]
  RooijGraphics2D
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
