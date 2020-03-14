(ns cardo.scene.start
  (:require
   [integrant.core :as ig]
   [essen.module.matterjs]))

(defmethod ig/init-key :scene/start [_ opts]
  opts)

(def config
  {[:essen/scene :scene/start]
   {:scene/entities [:entity/player]
    :matter (ig/ref :matterjs/start)}

   :matterjs/start {}})
