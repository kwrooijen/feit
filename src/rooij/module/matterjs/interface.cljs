(ns rooij.module.matterjs.interface
  (:require
   ["matter-js" :as Matter :refer [Engine]]
   [rooij.interface.physics-2d.core :refer [RooijPhysics2D]]
   [rooij.module.matterjs.interface.rectangle :as interface.rectangle]
   [rooij.module.matterjs.debug :as matterjs.debug]
   [rooij.module.matterjs.state :as state]))

(def target-fps (/ 1000 60))

(defrecord MatterPhysics2D [init-opts]
  RooijPhysics2D
  (scene-init [this scene-key]
    (state/init-engine! scene-key))

  (scene-halt! [this scene-key]
    (state/halt-engine! scene-key))

  (step [this scene-key delta]
    (when-let [engine (state/get-engine scene-key)]
      (doseq [_ (doall (range 0 (Math/floor (/ delta target-fps))))]
        (.update Engine engine target-fps 1))
      (.update Engine engine (rem delta target-fps) 1)))

  (get-wireframe-vectors [this scene-key]
    (matterjs.debug/wireframe-vectors scene-key))

  (make-rectangle [this opts]
    (interface.rectangle/make opts)))
