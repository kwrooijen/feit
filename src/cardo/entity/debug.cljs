(ns cardo.entity.debug
  (:require
   [essen.module.matterjs :as matter]
   [integrant.core :as ig]
   [essen.module.pixi.debug :as pixi.debug]))

(defmethod ig/init-key :entity/debug [_ opts] identity)
(defmethod ig/init-key :component/debug [_ opts] opts)

(defmethod ig/init-key :ticker/debug
  [_k {:context/keys [scene]}]
  (fn ticker-debug [_subs _component _ticker _state]
    (pixi.debug/draw-wireframe (matter/points) scene)))

(def config
  {[:essen/entity :entity/debug]
   {:entity/components
    [(ig/ref :component/debug)]

    [:essen/component :component/debug]
    {:component/tickers [(ig/ref :ticker/debug)]}

    [:essen/ticker :ticker/debug] {}}})
