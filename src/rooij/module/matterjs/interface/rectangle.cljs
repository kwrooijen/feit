(ns rooij.module.matterjs.interface.rectangle
  (:require
   ["matter-js" :as Matter :refer [Bodies World Body]]
   [rooij.interface.general-2d.position :refer [RooijGeneral2DPosition]]
   [rooij.interface.physics-2d.rectangle :refer [RooijPhysics2DRectangle]]
   [rooij.module.matterjs.state :as state]))

(defrecord MatterPhysics2DRectangle [body x y w h]
  RooijPhysics2DRectangle)

(extend-protocol RooijGeneral2DPosition
  MatterPhysics2DRectangle
  (set-position [{:keys [body] :as this} x y angle]
    (.setAngle Body body angle)
    (assoc this :x x :y y))
  (get-position [{:keys [body w h] :as this}]
    {:x (int (.. body -position -x))
     :y (int (.. body -position -y))
     :angle (.. body -angle)}))

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
    (when (false? (:auto-rotate opts))
      (.setInertia Body rectangle js/Infinity))
    (.add World (state/get-world scene-key) rectangle)
    (map->MatterPhysics2DRectangle
     {:body rectangle :x x :y y :w w :h h})))

(defn- existing-rectangle [{:context/keys [scene-key state]}]
  (.add World (state/get-world scene-key) (:body state))
  state)

(defn make [opts]
  (if (:context/state opts)
    (existing-rectangle opts)
    (new-rectangle opts)))
