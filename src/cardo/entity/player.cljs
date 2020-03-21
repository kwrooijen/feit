(ns cardo.entity.player
  (:require
   [essen.system.component :as component]
   [essen.core :refer [emit!]]
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/player [_ _]
  (fn entity--player [config]
    (-> config
        (assoc-in [:matterjs.component/rectangle :component/x] 380)
        (assoc-in [:matterjs.component/rectangle :component/y] 100))))

(defmethod component/persistent-resume :component.player/position
  [_key {:context/keys [scene entity]} {:position/keys [x y] :as state}]
  ;; TODO separate opts and context
  (emit! scene entity :handler.pixi.sprite/set-pos {:event/x x :event/y y})
  state)

(defmethod ig/init-key :ticker.player/position [_ {:context/keys [scene entity]}]
  (fn ticker-player--position
    [_subs _component _tick _state
     {:matterjs.component/keys [rectangle]}]
    (let [body ((:component/body rectangle))
          event {:event/x (.. body -position -x)
                 :event/y (.. body -position -y)}]
      (emit! scene entity :handler.pixi.sprite/set-pos event)
      (emit! scene entity :handler.pixi.sprite/set-rotation {:event/rotation (.-angle body)}))))

(def config
  {[:essen/entity :entity/player]

   {:entity/components
    [(ig/ref :component/stats)
     (ig/ref :component.pixi/sprite)
     (ig/ref :component/position)
     (ig/ref :matterjs.component/rectangle)]

    :entity/subs {:entity/monster [:component/stats :component/equipment]}

    :component/stats
    {:component/persistent true
     :stats/hp 3}

    :component/position
    {:position/x 200
     :position/y 300
     :component/tickers [(ig/ref :ticker.player/position)]
     :component/reactors []}

    :component.pixi/sprite
    {:component/sprite [:spritesheet "adventurer-idle"]
     :component/pos {:x 200 :y 300}}

    :matterjs.component/rectangle
    {:component/x 400
     :component/y 200
     :component/width 20
     :component/height 31
     :component.opts/restitution 1}

    [:essen/ticker :ticker.player/position] {}}})
