(ns cardo.config
  (:require
   [cardo.entity.yeti :as entity.yeti]
   [cardo.entity.skeleton :as entity.skeleton]
   [cardo.entity.player :as entity.player]

   [cardo.scene.load :as scene.load]
   [cardo.scene.start :as scene.start]
   [cardo.scene.battle :as scene.battle]

   [cardo.component.stats :as component.stats]
   [cardo.component.equipment :as component.equipment]

   [essen.module.pixi]))

(def debug?
  ^boolean goog.DEBUG)

(def config
  (merge
   {:game/renderer {:renderer/view "game"
                    :renderer/width (.-innerWidth js/window)
                    :renderer/height (.-innerHeight js/window)
                    :renderer/resolution (.-devicePixelRatio js/window)
                    :renderer/autoDencity true}}



   scene.load/config
   scene.start/config
   scene.battle/config

   entity.player/config
   entity.skeleton/config
   entity.yeti/config

   component.stats/config
   component.equipment/config

   essen.module.pixi/config))
