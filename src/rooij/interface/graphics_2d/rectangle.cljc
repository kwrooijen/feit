(ns rooij.interface.graphics-2d.rectangle
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [rooij.config]
   [rooij.dsl :as r]
   [rooij.interface.graphics-2d.core :refer [make-rectangle]]
   [rooij.state :as state]))

(defprotocol RooijGraphics2DRectangle
  (halt! [this]))

(defmethod ig/init-key :graphics-2d.component/rectangle [_k opts]
  (make-rectangle state/graphics-2d opts))

(defmethod ig/halt-key! :graphics-2d.component/rectangle [_ _opts]
  (fn [state]
    (halt! state)))

(it/derive-hierarchy
 {:graphics-2d.component/rectangle [:rooij/component :rooij/position]})

(-> (r/component :graphics-2d.component/rectangle)
    (r/ref-handler :general-2d.handler.position/set)
    (r/save-interface!))
