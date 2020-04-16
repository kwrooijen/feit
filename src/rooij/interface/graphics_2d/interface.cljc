(ns rooij.interface.graphics-2d.interface)

(defprotocol RooijGraphics2D
  (scene-init [this scene-key])
  (scene-halt! [this scene-key])
  (step [this scene-key])
  (draw-wireframe [this scene-key vectors])
  (make-loader [this opts])
  (make-sprite [this opts])
  (make-rectangle [this opts]))

(defprotocol RooijGraphics2DSprite
  (play! [this spritesheet animation]))

(defprotocol RooijGraphics2DRectangle)

(deftype DefaultGraphics2D []
  RooijGraphics2D
  (scene-init [this scene-key] nil)
  (scene-halt! [this scene-key] nil)
  (step [this scene-key] nil)
  (draw-wireframe [this scene-key vectors] nil))
