(ns cardo.scene.start
  (:require
   [integrant.core :as ig]
   [essen.module.matterjs]))

(defmethod ig/init-key :scene/start [_ opts]
  opts)

(def config
  {[:essen/scene :scene/start]
   {:scene/entities [:entity/player
                     :entity/debug
                     :entity.start/circle
                     :entity.start/ground]}

   [:matterjs.component/circle :entity.start/circle]
   {:component/x 400
    :component/y 200
    :component/radius 100
    :component.opts/restitution 0.5
    :component.opts/friction 0
    :component.opts/frictionAir 0}

   [:matterjs.component/rectangle :entity.start/ground]
   {:component/x 400
    :component/y 600
    :component/width 800
    :component/height 60
    :component.opts/isStatic true
    :component.opts/restitution 1}})
