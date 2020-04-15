(ns rooij.module.matterjs.core
  (:require
   ["matter-js" :as Matter :refer [Engine]]
   [integrant.core :as ig]
   [rooij.interface.physics-2d.core :refer [RooijPhysics2D]]
   [rooij.module.matterjs.state :as state]))

(deftype MatterPhysics2D []
  RooijPhysics2D
  (scene-init [this scene-key]
    (state/init-engine! scene-key))
  (scene-halt! [this scene-key]
    (state/halt-engine! scene-key))
  (step [this scene-key delta]
    (.update Engine (state/get-engine scene-key) delta 1)))

(defmethod ig/init-key :rooij.interface.physics-2d/system [_ _opts]
  (->MatterPhysics2D))
