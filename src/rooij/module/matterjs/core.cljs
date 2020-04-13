(ns rooij.module.matterjs.core
  (:require
   ["matter-js" :as Matter :refer [Engine]]
   [integrant.core :as ig]
   [rooij.module.matterjs.state :as state]))

(defmethod ig/init-key :rooij.interface.physics-2d/scene [_ _opts]
  (fn interface-physics-2d--scene-init  [scene-key]
    (state/init-engine! scene-key)))

(defmethod ig/halt-key! :rooij.interface.physics-2d/scene [_ _opts]
  (fn interface-physics-2d--scene-halt! [scene-key]
    (state/halt-engine! scene-key)))

(defmethod ig/init-key :rooij.interface.physics-2d/system [_ _opts]
  (fn [delta scene-key]
    (.update Engine (state/get-engine scene-key) delta 1)))
