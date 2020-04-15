(ns rooij.interface.physics-2d.core
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.config]
   [rooij.state :as state]))

(defprotocol RooijPhysics2D
  (scene-init [this scene-key])
  (scene-halt! [this scene-key])
  (step [this scene-key delta]))

(deftype DefaultPhysics2D []
  RooijPhysics2D
  (scene-init [this scene-key] nil)
  (scene-halt! [this scene-key] nil)
  (step [this scene-key delta] nil))

(def system
  :rooij.interface.physics-2d/system)

(defn init []
  (-> @rooij.config/config
      (meta-merge {system {}})
      (ig/prep [system])
      (ig/init [system])
      (it/find-derived-value system)
      (or (DefaultPhysics2D.))
      (state/set-physics-2d!)))

(it/derive-hierarchy
 {:physics-2d.component/rectangle [:rooij/component]})

(rooij.config/merge-interface!
 {:rooij.interface.physics-2d/system {}
  :rooij.interface.physics-2d/scene {}})
