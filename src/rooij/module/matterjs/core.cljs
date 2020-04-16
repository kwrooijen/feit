(ns rooij.module.matterjs.core
  (:require
   ["matter-js" :as Matter :refer [Engine Bodies World Body]]
   [integrant.core :as ig]
   [rooij.interface.general-2d.core :refer [RooijGeneral2DPosition]]
   [rooij.interface.physics-2d.core :refer [RooijPhysics2D RooijPhysics2DRectangle]]
   [rooij.module.matterjs.debug :as matterjs.debug]
   [rooij.module.matterjs.state :as state]))

(defrecord MatterPhysics2DRectangle [body x y w h]
  RooijPhysics2DRectangle)

(extend-protocol RooijGeneral2DPosition
  MatterPhysics2DRectangle
  (set-position [{:keys [body] :as this} x y]
    (assoc this :x x :y y))
  (get-position [{:keys [body w h] :as this}]
    {:x (int (.. body -position -x))
     :y (int (.. body -position -y))}))

(extend-protocol IPrintWithWriter
  ;; If a MatterJS body is printed using `println` a `too much recursion` error
  ;; occurs. We implement `IPrintWithWriter` to prevent this.
  MatterPhysics2DRectangle
  (-pr-writer [this writer _]
    (write-all writer (str "MatterPhysics2DRectangle#" (into {} (assoc this :body (symbol "#MatterJs/Body")))))))

(defn- opts->rectangle [{:keys [x y w h static]}]
  (.rectangle Bodies x y w h #js {:isStatic static}))

(defn- new-rectangle [{:context/keys [scene-key] :keys [x y w h] :as opts}]
  (let [rectangle (opts->rectangle opts)]
    (.add World (state/get-world scene-key) rectangle)
    (map->MatterPhysics2DRectangle
     {:body rectangle :x x :y y :w w :h h})))

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
