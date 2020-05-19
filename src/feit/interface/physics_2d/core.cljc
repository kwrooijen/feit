(ns feit.interface.physics-2d.core
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [feit.config]
   [feit.core.state :as state]))

(defprotocol FeitPhysics2D
  (scene-init [this scene-key])
  (scene-halt! [this scene-key])
  (step [this scene-key delta])
  (make-rectangle [this k opts])
  (get-wireframe-vectors [this scene-key]))

(deftype DefaultPhysics2D []
  FeitPhysics2D
  (scene-init [this scene-key] nil)
  (scene-halt! [this scene-key] nil)
  (step [this scene-key delta] nil))

(def system
  :feit.interface.physics-2d/system)

(defmethod ig/init-key :feit.interface.physics-2d/system [_ _]
  (DefaultPhysics2D.))

(defn init []
  (-> @feit.config/config
      (meta-merge {system {}})
      (ig/prep [system])
      (ig/init [system])
      (it/find-derived-value system)
      (state/set-physics-2d!)))
