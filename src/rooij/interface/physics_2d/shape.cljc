(ns rooij.interface.physics-2d.shape)

(defprotocol RooijPhysics2DShape
  (get-velocity [this])
  (set-velocity! [this xy])
  (add-velocity! [this xy]))
