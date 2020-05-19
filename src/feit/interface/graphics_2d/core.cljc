(ns feit.interface.graphics-2d.core
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [feit.config]
   [feit.core.state :as state]))

(defprotocol FeitGraphics2D
  (scene-init [this scene-key])
  (scene-halt! [this scene-key])
  (step [this scene-key])
  (draw-wireframe [this scene-key vectors])
  (make-loader [this opts])
  (make-sprite [this opts])
  (make-rectangle [this opts]))

(deftype DefaultGraphics2D []
  FeitGraphics2D
  (scene-init [this scene-key] nil)
  (scene-halt! [this scene-key] nil)
  (step [this scene-key] nil)
  (draw-wireframe [this scene-key vectors] nil))

(def system
  :feit.interface.graphics-2d/system)

(defmethod ig/init-key :feit.interface.graphics-2d/system [_ _]
  (DefaultGraphics2D.))

(defn init []
  (-> @feit.config/config
      (meta-merge {system {}})
      (ig/prep [system])
      (ig/init [system])
      (it/find-derived-value system)
      (state/set-graphics-2d!)))
