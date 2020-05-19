(ns feit.interface.physics-2d.shape
  (:require
   [feit.dsl :as r]
   [integrant.core :as ig]))

(defprotocol FeitPhysics2DShape
  (get-velocity [this])
  (set-velocity [this xy])
  (set-velocity-x [this x])
  (set-velocity-y [this y])
  (add-velocity [this xy]))

(defmethod ig/init-key :physics-2d.handler.shape/set-velocity [_ _opts]
  (fn handler-physics-2d-handler--set-velocity
    [_context event state]
    (set-velocity state event)))

(defmethod ig/init-key :physics-2d.handler.shape/set-velocity-x [_ _opts]
  (fn handler-physics-2d-handler--set-velocity-x
    [_context event state]
    (set-velocity-x state event)))

(defmethod ig/init-key :physics-2d.handler.shape/set-velocity-y [_ _opts]
  (fn handler-physics-2d-handler--set-velocity-y
    [_context event state]
    (set-velocity-y state event)))

(defmethod ig/init-key :physics-2d.handler.shape/add-velocity [_ _opts]
  (fn handler-physics-2d-handler--add-velocity
    [_context event state]
    (add-velocity state event)))

(-> (r/component :physics-2d.component/rectangle)
    (r/handler+ref :physics-2d.handler.shape/set-velocity)
    (r/handler+ref :physics-2d.handler.shape/set-velocity-x)
    (r/handler+ref :physics-2d.handler.shape/set-velocity-y)
    (r/handler+ref :physics-2d.handler.shape/add-velocity)
    (r/ref-handler :general-2d.handler.position/set)
    (r/save-interface!))
