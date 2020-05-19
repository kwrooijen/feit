(ns feit.interface.graphics-2d.rectangle
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [feit.config]
   [feit.dsl :as r]
   [feit.interface.graphics-2d.core :refer [make-rectangle]]
   [feit.state :as state]))

(defprotocol FeitGraphics2DRectangle
  (halt! [this]))

(defmethod ig/init-key :graphics-2d.component/rectangle [_k opts]
  (make-rectangle state/graphics-2d opts))

(defmethod ig/halt-key! :graphics-2d.component/rectangle [_ _opts]
  (fn [state]
    (halt! state)))

(it/derive-hierarchy
 {:graphics-2d.component/rectangle [:feit/component :feit/position]})

(-> (r/component :graphics-2d.component/rectangle)
    (r/ref-handler :general-2d.handler.position/set)
    (r/save-interface!))
