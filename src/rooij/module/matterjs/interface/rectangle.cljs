(ns rooij.module.matterjs.interface.rectangle
  (:require
   [clojure.core.matrix :as m]
   ["matter-js" :as Matter :refer [Bodies World Body]]
   [rooij.interface.general-2d.position :refer [RooijGeneral2DPosition]]
   [rooij.interface.physics-2d.rectangle :refer [RooijPhysics2DRectangle]]
   [rooij.interface.physics-2d.shape :refer [RooijPhysics2DShape]]
   [rooij.module.matterjs.state :as state]))

(defn matrix->matter-vector [[x y]]
  #js {:x x :y y})

(defn -get-velocity [{:keys [body]}]
  (let [velocity (.-velocity body)]
    [(.-x velocity)
     (.-y velocity)]))

(defrecord MatterPhysics2DRectangle [body x y w h pivot-x pivot-y]
  RooijPhysics2DRectangle)

(extend-protocol RooijGeneral2DPosition
  MatterPhysics2DRectangle
  (set-position [{:keys [body pivot-x pivot-y] :as this} x y angle]
    (.setPosition Body body #js {:x (+ x pivot-x)
                                 :y (+ y pivot-y)})
    (.setAngle Body body angle)
    (assoc this :x x :y y))
  (get-position [{:keys [body w h] :as this}]
    {:x (int (.. body -position -x))
     :y (int (.. body -position -y))
     :angle (.. body -angle)}))

(extend-protocol RooijPhysics2DShape
  MatterPhysics2DRectangle
  (get-velocity [this]
    (-get-velocity this))

  (set-velocity! [{:keys [body]} xy]
    (.setVelocity Body body (matrix->matter-vector xy)))

  (set-velocity-x! [{:keys [body]} x]
    (.setVelocity Body body #js {:x x :y (.. body -velocity -y)}))

  (set-velocity-y! [{:keys [body]} y]
    (.setVelocity Body body #js {:x (.. body -velocity -x) :y y}))

  (add-velocity! [{:keys [body] :as this} xy]
    (.setVelocity Body body
                  (-> (-get-velocity this)
                      (m/add xy)
                      (matrix->matter-vector)))))

(extend-protocol IPrintWithWriter
  ;; If a MatterJS body is printed using `println` a `too much recursion` error
  ;; occurs. We implement `IPrintWithWriter` to prevent this.
  MatterPhysics2DRectangle
  (-pr-writer [this writer _]
    (write-all writer (str "MatterPhysics2DRectangle#" (into {} (assoc this :body (symbol "#MatterJs/Body")))))))

(defn- opts->rectangle [{:keys [x y w h static? sensor?]
                         pivot-x :pivot/x
                         pivot-y :pivot/y}]
  (.rectangle Bodies (+ x pivot-x) (+ y pivot-y) w h
              #js {:isStatic static? :isSensor sensor?}))

(defn- new-rectangle [k {:context/keys [scene-key entity-key]
                         :keys [x y w h angle]
                         pivot-x :pivot/x
                         pivot-y :pivot/y
                         :or {angle 0}
                         :as opts}]
  (let [rectangle (opts->rectangle opts)]
    (when (false? (:auto-rotate opts))
      (.setInertia Body rectangle js/Infinity))
    (.add World (state/get-world scene-key) rectangle)
    (.setAngle Body rectangle angle)
    (set! (.-component-key rectangle) k)
    (set! (.-entity-key rectangle) entity-key)
    (set! (.-scene-key rectangle) scene-key)
    (map->MatterPhysics2DRectangle
     {:body rectangle :x x :y y :w w :h h :pivot-x pivot-x :pivot-y pivot-y})))

(defn- existing-rectangle [{:context/keys [scene-key state]}]
  (.add World (state/get-world scene-key) (:body state))
  state)

(defn make [k opts]
  (if (:context/state opts)
    (existing-rectangle opts)
    (new-rectangle k opts)))
