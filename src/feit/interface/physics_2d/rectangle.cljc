(ns feit.interface.physics-2d.rectangle
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [feit.dsl :as r]
   [feit.interface.physics-2d.core :refer [make-rectangle]]
   [feit.core.state :as state]))

(defprotocol FeitPhysics2DRectangle)

(defmethod ig/init-key :physics-2d.component/rectangle [k opts]
  (make-rectangle state/physics-2d k opts))

(it/derive-hierarchy
 {:physics-2d.component/rectangle [:feit/component :feit/position]})

(-> (r/component :physics-2d.component/rectangle)
    (r/ref-handler :general-2d.handler.position/set)
    (r/save-interface!))
