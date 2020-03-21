(ns cardo.scene.start
  (:require
   [integrant.core :as ig]
   [essen.module.matterjs]))

(defmethod ig/init-key :scene/start [_ opts]
  opts)

(defmethod ig/init-key :entity.start/ball [_ opts] identity)
(defmethod ig/init-key :entity.start/ground [_ opts] identity)

(def config
  {[:essen/scene :scene/start]
   {:scene/entities [:entity/player
                     :entity/debug
                     :entity.start/ground
                     :entity.start/ball]}

   [:essen/entity :entity.start/ball]
   {:entity/components [(ig/ref :component.start/ball)]}

   [:matterjs.component/circle :component.start/ball]
   {:component/x 400
    :component/y 200
    :component/radius 100
    :component.opts/restitution 0.5
    :component.opts/friction 0
    :component.opts/frictionAir 0}


   [:essen/entity :entity.start/ground]
   {:entity/components [(ig/ref :component.start/ground)]

    [:matterjs.component/rectangle :component.start/ground]
    {:component/x 400
     :component/y 600
     :component/width 800
     :component/height 60
     :component.opts/isStatic true
     :component.opts/restitution 1}}})
