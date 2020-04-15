(ns rooij.module.matterjs.core
  (:require
   ["matter-js" :as Matter :refer [Engine Bodies World Events Mouse MouseConstraint]]
   [integrant.core :as ig]
   [rooij.interface.physics-2d.core :refer [RooijPhysics2D RooijPhysics2DRectangle]]
   [rooij.module.matterjs.state :as state]
   [rooij.module.matterjs.debug :as matterjs.debug]))

(defrecord MatterPhysics2DRectangle [body]
  RooijPhysics2DRectangle)

(extend-protocol IPrintWithWriter
  MatterPhysics2DRectangle
  (-pr-writer [this writer _]
    (write-all writer (into {} (assoc this :body (symbol "#MatterJs/Body"))))))

(defn -make-rectangle [{:context/keys [scene-key] :keys [x y w h static]}]
  (let [rectangle (.rectangle Bodies (+ x (/ w 2)) (+ y (/ h 2)) w h
                              #js {:isStatic static})]
    (.add World (state/get-world scene-key) rectangle)
    (map->MatterPhysics2DRectangle
     {:body rectangle})))

(defrecord MatterPhysics2D [init-opts]
  RooijPhysics2D
  (scene-init [this scene-key]
    (state/init-engine! scene-key))
  (scene-halt! [this scene-key]
    (state/halt-engine! scene-key))
  (step [this scene-key delta]
    (.update Engine (state/get-engine scene-key) delta 1))
  (get-wireframe-vectors [this scene-key]
    (matterjs.debug/wireframe-vectors scene-key))
  (make-rectangle [this opts]
    (-make-rectangle opts)))

(defmethod ig/init-key :rooij.interface.physics-2d/system [_ init-opts]
  (->MatterPhysics2D init-opts))
