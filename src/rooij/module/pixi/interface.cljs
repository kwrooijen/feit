(ns rooij.module.pixi.interface
  (:require
   [rooij.interface.graphics-2d.core :refer [RooijGraphics2D]]
   [rooij.module.pixi.debug :as pixi.debug]
   [rooij.module.pixi.entity]
   [rooij.module.pixi.state :as state]
   [rooij.module.pixi.interface.sprite :as interface.sprite]
   [rooij.module.pixi.interface.rectangle :as interface.rectangle]))

(deftype PixiGraphics2D [init-opts]
  RooijGraphics2D
  (scene-init [this scene-key]
    (state/init-scene! scene-key))
  (scene-halt! [this scene-key]
    (state/halt-scene! scene-key) )
  (step [this scene-key]
    (.render state/renderer (state/get-scene scene-key)))
  (draw-wireframe [this scene-key vectors]
    (pixi.debug/draw-wireframe scene-key vectors))
  (make-sprite [this opts] (interface.sprite/make opts))
  (make-rectangle [this opts] (interface.rectangle/make opts)))
