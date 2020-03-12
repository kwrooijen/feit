(ns cardo.entity.player
  (:require
   [essen.core :refer [emit!]]
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/player [_ opts]
  opts)

;; Custom reactor added to an existing component
(defmethod ig/init-key :reactor.player.position/update-sprite [_ _opts]
  (fn [{:context/keys [scene entity]} _event _old-state {:position/keys [x y]}]
    (emit! scene entity :handler.pixi.sprite/set-pos {:event/x x :event/y y})))

(def config
  {[:component/stats :component.player/stats]
   {:stats/hp 3}

   [:component.pixi/sprite :component.player/pixi.sprite]
   {:component/sprite [:spritesheet "adventurer-idle"]
    :component/pos {:x 200 :y 300}}

   [:essen/reactor :reactor.player.position/update-sprite] {}

   [:component/position :component.player/position]
   {:position/x 200
    :position/y 300
    :component/reactors [(ig/ref :reactor.player.position/update-sprite)]}
  
   ^:persistent
   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component.player/stats)
     (ig/ref :component.player/pixi.sprite)
     (ig/ref :component.player/position)]
    :entity/subs {:entity/monster [:component/stats :component/equipment]}}})
