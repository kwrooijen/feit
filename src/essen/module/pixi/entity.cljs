(ns essen.module.pixi.entity
  (:require
   [essen.core :as essen]
   [essen.module.pixi.state :as state]
   [integrant.core :as ig]))

(defmethod ig/init-key :graphics-2d.entity/asset-loader
  [_ {:g2d.asset-loader/keys [files prefix next-scene]
      :context/keys [scene-key]}]
  (doseq [file files]
    (.add state/loader file))
  (.load state/loader #(essen/transition-scene scene-key next-scene)))
