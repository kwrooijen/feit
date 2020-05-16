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
  (set-position [{:keys [rectangle pivot-x pivot-y] :as this} x y angle]
    (.setPosition Body rectangle #js {:x (+ x pivot-x)
                                 :y (+ y pivot-y)})
    (.setAngle Body rectangle angle)
    (assoc this :x x :y y))
  (get-position [{:keys [rectangle w h] :as this}]
    {:x (int (.. rectangle -position -x))
     :y (int (.. rectangle -position -y))
     :angle (.. rectangle -angle)}))

(extend-protocol RooijPhysics2DShape
  MatterPhysics2DRectangle
  (get-velocity [this]
    (-get-velocity this))

  (set-velocity [{:keys [body] :as this} xy]
    (.setVelocity Body body (matrix->matter-vector xy))
    this)

  (set-velocity-x [{:keys [body] :as this} x]
    (.setVelocity Body body #js {:x x :y (.. body -velocity -y)})
    this)

  (set-velocity-y [{:keys [body] :as this} y]
    (.setVelocity Body body #js {:x (.. body -velocity -x) :y y})
    this)

  (add-velocity [{:keys [body] :as this} xy]
    (.setVelocity Body body
                  (-> (-get-velocity this)
                      (m/add xy)
                      (matrix->matter-vector)))
    this))

(extend-protocol IPrintWithWriter
  ;; If a MatterJS body is printed using `println` a `too much recursion` error
  ;; occurs. We implement `IPrintWithWriter` to prevent this.
  MatterPhysics2DRectangle
  (-pr-writer [this writer _]
    (write-all writer (str "MatterPhysics2DRectangle#" (into {} (assoc this :body (symbol "#MatterJs/Body")))))))

(defn add-context-keys
  [o {:context/keys [component-key entity-key scene-key]}]
  (set! (.-component-key o) component-key)
  (set! (.-entity-key o) entity-key)
  (set! (.-scene-key o) scene-key)
  (set! (.-context o) {:context/component-key component-key
                       :context/entity-key entity-key
                       :context/scene-key scene-key})
  o)

(defn- opts->rectangle
  [{:keys [x y w h static? sensor?]
    pivot-x :pivot/x
    pivot-y :pivot/y
    :as opts}]
  (let [rectangle (.rectangle Bodies (+ x pivot-x) (+ y pivot-y) w h
                              #js {:isStatic static? :isSensor sensor?})]
    (add-context-keys rectangle opts)))

(defn- set-inertia
  "When the :rotation? key inside of `opts` is set to `false`, disable any auto
  rotation by matterjs."
  [o opts]
  (when (false? (:rotation? opts))
    (.setInertia Body o js/Infinity))
  o)

(defn add-sensor-keys
  ""
  [sensor {:sensor/keys [key opts]}]
  (set! (.-collision-target sensor) (:sensor.collision/target opts))
  (set! (.-collision-handler sensor) (:sensor.collision/handler opts))
  (set! (.-sensor-key sensor) key)
  sensor)

(defn- add-sensor-to-parts
  ""
  [{:keys [x y]
    :as opts}
   {sensor-x :sensor/x
    sensor-y :sensor/y
    sensor-w :sensor/w
    sensor-h :sensor/h
    :as sensor-opts}]
  (let [sensor-x (+ x sensor-x)
        sensor-y (+ y sensor-y)
        sensor-rectangle-opts #js {:isSensor true}]
    (-> (.rectangle Bodies sensor-x sensor-y sensor-w sensor-h sensor-rectangle-opts)
        (add-sensor-keys sensor-opts)
        (add-context-keys opts))))

(defn- opts->rectangle-map
  ""
  [{:keys [x y w h]
    pivot-x :pivot/x
    pivot-y :pivot/y}]
  {:x x :y y :w w :h h :pivot-x pivot-x :pivot-y pivot-y})

(defn- opts->rectangle-body
  ""
  [{:keys [angle static?]
    :or {angle 0}
    :as opts}]
  (let [rectangle (opts->rectangle opts)
        parts (map (partial add-sensor-to-parts opts) (:sensors opts))]
    (set-inertia rectangle opts)
    (.setAngle Body rectangle angle)
    (-> (.create Body (clj->js {:parts (cons rectangle parts)
                                :isStatic static?}))
        (add-context-keys opts))))

(defn- new-rectangle
  [_ {:context/keys [scene-key]
      :as opts}]
  (let [body (opts->rectangle-body opts)]
    (.add World (state/get-world scene-key) body)
    (map->MatterPhysics2DRectangle
     (-> (opts->rectangle-map opts)
         (assoc :body body :rectangle (-> body (.-parts ) (aget 0)))))))

(defn- existing-rectangle [{:context/keys [scene-key state]}]
  (.add World (state/get-world scene-key) (:body state))
  state)

(defn make [k opts]
  (if (:context/state opts)
    (existing-rectangle opts)
    (new-rectangle k opts)))
