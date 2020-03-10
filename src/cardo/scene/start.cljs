(ns cardo.scene.start
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :scene/start [_ opts]
  opts)

(def config
  {[:essen/scene :scene/start]
   {:scene/entities [:entity/player]}})