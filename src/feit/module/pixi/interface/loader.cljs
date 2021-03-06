(ns feit.module.pixi.interface.loader
  (:require
   ["pixi.js" :as PIXI]
   [feit.interface.graphics-2d.loader :as interface.loader :refer [FeitGraphics2DLoader]]
   [feit.module.pixi.state :as state]))

(defn- spritesheet-loaded
  [context file name loader]
  (-> loader
      (.-resources)
      (aget file)
      (->> (state/add-spritesheet! name)))
  (interface.loader/load-complete! context))

(defn- texture-loaded [context file name loader]
  (state/add-texture! name (-> loader .-resources (aget file) .-texture))
  (interface.loader/load-complete! context))

(defrecord PixiGraphics2DLoader [opts]
  FeitGraphics2DLoader

  (load-texture [this context file name]
    (-> (PIXI/Loader.)
        (.add file)
        (.load (partial texture-loaded context file name))))

  (load-spritesheet [this context file name]
    (-> (PIXI/Loader.)
        (.add file)
        (.load (partial spritesheet-loaded context file name)))))

(defn make [opts]
  (->PixiGraphics2DLoader opts))
