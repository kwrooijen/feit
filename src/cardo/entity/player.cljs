(ns cardo.entity.player
  (:require
   [meta-merge.core :refer [meta-merge]]
   [essen.core :refer [emit!]]
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/player [_ opts]
  opts)

(defmethod ig/prep-key :component/position [_ opts]
  (meta-merge
   opts
   {:component/handlers
    [(ig/ref :handler.position/move)]

    :component/reactors
    [(ig/ref :reactor.position/update-sprite)]

    :component/tickers
    []}))

(defmethod ig/init-key :component.player/position [_ _opts]
  {:position/x 0
   :position/y 0})

(defmethod ig/init-key :handler.position/move [_ _opts]
  (fn [_context {:event/keys [x y] :as _event} state]
    (-> state
        (update :position/x + x)
        (update :position/y + y))))

(defmethod ig/init-key :reactor.position/update-sprite [_ _opts]
  (fn [{:context/keys [scene entity]} _event _old-state {:position/keys [x y]}]
    (emit! scene entity :handler.pixi.sprite/set-pos {:event/x x :event/y y})))

(derive :component/position :essen/component)
(def config
  {[:component/stats :component.player/stats]
   {:stats/hp 3}

   [:component.pixi/sprite :component.player.pixi/sprite]
   {:component/sprite [:spritesheet "adventurer-idle"]
    :component/pos {:x 200 :y 300}
    :component/handlers []}

   [:essen/reactor :reactor.position/update-sprite] {}
   [:essen/handler :handler.position/move] {}

   [:component/position :component.player/position] {}
  
   ^:persistent
   [:essen/entity :entity/player]
   {:entity/components
    [(ig/ref :component.player/stats)
     (ig/ref :component.player.pixi/sprite)
     (ig/ref :component.player/position)]
    :entity/subs {:entity/monster [:component/stats :component/equipment]}}})
