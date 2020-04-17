(ns rooij.interface.physics-2d.shape)

(defprotocol RooijPhysics2DShape
  (get-velocity [this])
  (set-velocity! [this xy])
  (set-velocity-x! [this x])
  (set-velocity-y! [this y])
  (add-velocity! [this xy]))
