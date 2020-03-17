(ns cardo.entity.player
  (:require
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
  (fn [{:context/keys [scene] :as context} _delta _time _state]
    (when @m/box
      (let [event {:event/x (.. @m/box -position -x)
                   :event/y (.. @m/box -position -y)}]
        (emit! context :handler.pixi.sprite/set-pos event)
        (emit! context :handler.pixi.sprite/set-rotation {:event/rotation (.-angle @m/box)})))))

(def config
  {^:persistent
   [:component/stats :component.player/stats]
   {:stats/hp 3}

   [:component.pixi/sprite :component.player/pixi.sprite]
   {:component/sprite [:spritesheet "adventurer-idle"]
    :component/pos {:x 200 :y 300}}

   [:essen/ticker :ticker.player/position] {}

   [:essen/reactor :reactor.player.position/update-sprite] {}

   ^:persistent
   [:component/position :component.player/position]
   {:position/x 200
    :position/y 300
    :component/tickers [(ig/ref :ticker.player/position)]
    :component/reactors []}
  
   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component.player/stats)
     (ig/ref :component.player/pixi.sprite)
     (ig/ref :component.player/position)]
    :entity/subs {:entity/monster [:component/stats :component/equipment]}}})
