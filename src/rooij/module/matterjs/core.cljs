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
  ;; If a MatterJS body is printed using `println` a `too much recursion` error
  ;; occurs. We implement `IPrintWithWriter` to prevent this.
  MatterPhysics2DRectangle
  (-pr-writer [this writer _]
    (write-all writer (into {} (assoc this :body (symbol "#MatterJs/Body"))))))

(defn- opts->rectangle [{:keys [x y w h static]}]
  (.rectangle Bodies (+ x (/ w 2)) (+ y (/ h 2)) w h #js {:isStatic static}))

(defn- new-rectangle [{:context/keys [scene-key] :as opts}]
  (let [rectangle (opts->rectangle opts)]
    (.add World (state/get-world scene-key) rectangle)
    (map->MatterPhysics2DRectangle
     {:body rectangle})))

(defn- existing-rectangle [{:context/keys [scene-key state]}]
  (.add World (state/get-world scene-key) (:body state))
  state)

(defn -make-rectangle [opts]
  (if (:context/state opts)
    (existing-rectangle opts)
    (new-rectangle opts)))

(def target-fps (/ 1000 60))

(defrecord MatterPhysics2D [init-opts]
  RooijPhysics2D
  (scene-init [this scene-key]
    (state/init-engine! scene-key))
  (scene-halt! [this scene-key]
    (state/halt-engine! scene-key))
  (step [this scene-key delta]
    (let [engine (state/get-engine scene-key)]
      (doseq [_ (doall (range 0 (Math/floor (/ delta target-fps))))]
        (.update Engine engine target-fps 1))
      (.update Engine engine (rem delta target-fps) 1)))
  (get-wireframe-vectors [this scene-key]
    (matterjs.debug/wireframe-vectors scene-key))
  (make-rectangle [this opts]
    (-make-rectangle opts)))

(defmethod ig/init-key :rooij.interface.physics-2d/system [_ init-opts]
  (->MatterPhysics2D init-opts))
