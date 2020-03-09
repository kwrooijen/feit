(ns cardo.entity.player
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(def config
  {[:component/stats :component.player/stats]
   {:stats/hp 40}

   ^:persistent
   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component.player/stats)]
    :entity/subs {:entity/monster [:component/stats :component/equipment]}}})
