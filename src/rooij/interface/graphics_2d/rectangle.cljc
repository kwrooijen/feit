(ns rooij.interface.graphics-2d.rectangle
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.config]
   [rooij.interface.graphics-2d.core :refer [make-rectangle]]
   [rooij.state :as state]))

(defprotocol RooijGraphics2DRectangle)

(defmethod ig/prep-key :graphics-2d.component/rectangle [_ opts]
  (meta-merge
   {:component/handlers [{:handler/ref (ig/ref :general-2d.handler.position/set)}]}
   opts))

(defmethod ig/init-key :graphics-2d.component/rectangle [k opts]
  (make-rectangle state/graphics-2d opts))

(it/derive-hierarchy
 {:graphics-2d.component/rectangle [:rooij/component :rooij/position]})

(rooij.config/merge-interface!
 {})
