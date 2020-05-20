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

;; TODO Currently we can either reference :physics-2d.component/rectangle
;; directly, or create a component that derives
;; from :reactor.battle/update-sprite-positions. This means that we needs to
;; maintain the handlers / middleware / reactors / tickers here, and in the
;; DSL. This is not desirable. Maybe we could create a :component/inherit key,
;; that inherits these systems from other
;; components. e.g. {:component/inherit [:physics-2d.component/rectangle]}. That
;; way we can just add the inherit key in the DSL, and this can stay. Or we can
;; inherit from ancestor keys?
(-> (r/component :physics-2d.component/rectangle)
    (r/ref-handler :general-2d.handler.position/set)
    (r/save-interface!))
