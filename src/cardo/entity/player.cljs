(ns cardo.entity.player
  (:require
   [essen.module.matterjs.component.rectangle]
   [essen.module.matterjs :as m]
   [essen.system.component :as component]
   [essen.core :refer [emit!]]
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/player [_ opts]
  opts)

(defmethod component/persistent-resume :component.player/position
  [_key opts {:position/keys [x y] :as state}]
  (emit!  (-> opts :scene/opts :scene/key)
          (-> opts :entity/opts :entity/key)
          :handler.pixi.sprite/set-pos {:event/x x :event/y y})
  state)

(defmethod ig/init-key :ticker.player/position [_ opts]
  (fn [{:context/keys [scene] :as context} _tick _state
       {:component.player/keys [rectangle]}]
    (let [body ((:component/body rectangle))
          event {:event/x (.. body -position -x)
                 :event/y (.. body -position -y)}]
      (emit! context :handler.pixi.sprite/set-pos event)
      (emit! context :handler.pixi.sprite/set-rotation {:event/rotation (.-angle body)}))))

(def config
  {^:persistent
   [:component/stats :component.player/stats]
   {:stats/hp 3}

   ^:persistent
   [:component/position :component.player/position]
   {:position/x 200
    :position/y 300
    :component/tickers [(ig/ref :ticker.player/position)]
    :component/reactors []}

   [:component.pixi/sprite :component.player/pixi.sprite]
   {:component/sprite [:spritesheet "adventurer-idle"]
    :component/pos {:x 200 :y 300}}

   [:matterjs.component/rectangle :component.player/rectangle]
   {:component/x 400
    :component/y 200
    :component/width 20
    :component/height 31
    :component.opts/restitution 1}

   [:essen/ticker :ticker.player/position] {}

   [:essen/reactor :reactor.player.position/update-sprite] {}

   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component.player/stats)
     (ig/ref :component.player/pixi.sprite)
     (ig/ref :component.player/position)
     (ig/ref :component.player/rectangle)]
    :entity/subs {:entity/monster [:component/stats :component/equipment]}}})
