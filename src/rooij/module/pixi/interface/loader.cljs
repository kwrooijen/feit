(ns rooij.module.pixi.interface.loader
  (:require
   ["pixi.js" :as PIXI]
   [rooij.interface.graphics-2d.loader :as interface.loader :refer [RooijGraphics2DLoader]]
   [rooij.module.pixi.state :as state]))

(defn- spritesheet-loaded
  [context file name loader]
  (-> loader
      (.-resources)
      (aget file)
      (->> (state/add-spritesheet! name)))
  (interface.loader/load-complete! context))

(defrecord PixiGraphics2DLoader [opts]
  RooijGraphics2DLoader

  (load-texture [this context file name]
    (-> (PIXI/Loader.)
        (.add file name)
        (.load #(interface.loader/load-complete! context))))

  (load-spritesheet [this context file name]
    (-> (PIXI/Loader.)
        (.add file name)
        (.load (partial spritesheet-loaded context file name)))))

(defn make [opts]
  (->PixiGraphics2DLoader opts))
