(ns cardo.scene.start
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :scene/start [_ opts]
  opts)

(def config
  {[:essen/scene :scene/start]
   {:scene/entities [:entity/player]

    :animation (ig/ref :my/animation)
    :pixi (ig/ref :my/pixi)}


   [:essen.module.pixi/add-animation :my/animation]
   {:essen.module.pixi/sprite [:spritesheet "adventurer-attack1"]
    :essen.module.pixi/scene :scene/start}

   [:essen.module.pixi/add-sprite :my/pixi]
   {:essen.module.pixi/sprite [:spritesheet "adventurer-attack1_00"]
    :essen.module.pixi/scene :scene/start}})
