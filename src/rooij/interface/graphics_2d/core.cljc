(ns rooij.interface.graphics-2d.core
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.config]
   [rooij.state :as state]))

(defprotocol RooijGraphics2D
  (scene-init [this scene-key])
  (scene-halt! [this scene-key])
  (step [this scene-key])
  (draw-wireframe [this scene-key vectors])
  (make-loader [this opts])
  (make-sprite [this opts])
  (make-rectangle [this opts]))

(deftype DefaultGraphics2D []
  RooijGraphics2D
  (scene-init [this scene-key] nil)
  (scene-halt! [this scene-key] nil)
  (step [this scene-key] nil)
  (draw-wireframe [this scene-key vectors] nil))

(def system
  :rooij.interface.graphics-2d/system)

(defn init []
  (-> @rooij.config/config
      (meta-merge {system {}})
      (ig/prep [system])
      (ig/init [system])
      (it/find-derived-value system)
      (or (DefaultGraphics2D.))
      (state/set-graphics-2d!)))

(it/derive-hierarchy
 {:graphics-2d.entity/asset-loader [:rooij/entity]
  :graphics-2d.entity/spritesheet-loader [:rooij/entity]

  :graphics-2d.component/sprite    [:rooij/component :rooij/position]
  :graphics-2d.component/rectangle [:rooij/component :rooij/position]})

(rooij.config/merge-interface!
 {[:rooij/handler :graphics-2d.handler.sprite/play] {}
  [:rooij/handler :graphics-2d.handler.loader/load-complete] {}
  [:rooij/handler :graphics-2d.handler.loader/load-texture] {}
  [:rooij/handler :graphics-2d.handler.loader/load-spritesheet] {}})
