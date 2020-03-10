(ns cardo.entity.player
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/player [_ opts]
  opts)

(def config
  {[:component/stats :component.player/stats]
   {:stats/hp 40}

   [:component.pixi/sprite :component.player.pixi/sprite]
   {:component.pixi/spritesheet "hoi"
    :component/handlers []}

   ^:persistent
   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component.player/stats)
     (ig/ref :component.player.pixi/sprite)]
    :entity/subs {:entity/monster [:component/stats :component/equipment]}}})
