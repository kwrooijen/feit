(ns essen.module.pixi
  (:require
   ["pixi.js" :as PIXI]
   [essen.module.pixi.render :as render]))

(def module
  {:essen/setup render/setup
   :essen/stage-start render/stage-start
   :essen/stage-stop render/stage-stop
   :essen/stage-suspend render/stage-suspend})
