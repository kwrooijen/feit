(ns cardo.config
  (:require
   [cardo.entity.yeti :as entity.yeti]
   [cardo.entity.skeleton :as entity.skeleton]
   [cardo.entity.player :as entity.player]
   [cardo.entity.debug :as entity.debug]

   [cardo.scene.load :as scene.load]
   [cardo.scene.start :as scene.start]
   [cardo.scene.battle :as scene.battle]

   [cardo.component.stats :as component.stats]
   [cardo.component.equipment :as component.equipment]
   [cardo.component.position :as component.position]

   [essen.module.matterjs]
   [essen.module.pixi]))

(def debug?
  ^boolean goog.DEBUG)

(def config
  (merge
   {:essen.module/pixi {:pixi.renderer/view "game"
                        :pixi.renderer/width (.-innerWidth js/window)
                        :pixi.renderer/height (.-innerHeight js/window)
                        :pixi.renderer/resolution (.-devicePixelRatio js/window)
                        :pixi.renderer/autoDencity true}

    :essen.module/matterjs {}}

   scene.load/config
   scene.start/config
   scene.battle/config

   entity.player/config
   entity.skeleton/config
   entity.yeti/config
   entity.debug/config

   component.stats/config
   component.equipment/config
   component.position/config

   essen.module.matterjs/config
   essen.module.pixi/config))
