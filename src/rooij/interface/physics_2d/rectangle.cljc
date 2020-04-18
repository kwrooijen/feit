(ns rooij.interface.physics-2d.rectangle
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.interface.physics-2d.core :refer [make-rectangle]]
   [rooij.config]
   [rooij.state :as state]))

(defprotocol RooijPhysics2DRectangle)

(defmethod ig/prep-key :physics-2d.component/rectangle
  [_ {:component.position/keys [emitter] :as opts}]
  (meta-merge
   {:component/tickers [(when emitter {:ticker/ref (ig/ref :general-2d.ticker.position/emitter)})]
    :component/handlers [{:handler/ref (ig/ref :general-2d.handler.position/set)}]}
   opts))

(defmethod ig/init-key :physics-2d.component/rectangle [k opts]
  (make-rectangle state/physics-2d k opts))

(it/derive-hierarchy
 {:physics-2d.component/rectangle [:rooij/component :rooij/position]})
