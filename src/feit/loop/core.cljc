(ns feit.loop.core
  (:require
   #?(:clj [clojure.core.async :as async])
   [feit.loop.event :as loop.event]
   [feit.loop.handler :as loop.handler]
   [feit.loop.keyboard :as loop.keyboard]
   [feit.loop.middleware :as loop.middleware]
   [feit.loop.reactor :as loop.reactor]
   [feit.loop.ticker :as loop.ticker]
   [feit.system.scene :as system.scene]
   [feit.core.state :as state :refer [get-scene]]
   [feit.interface.graphics-2d.core :as interface.graphics-2d :refer [draw-wireframe]]
   [feit.interface.physics-2d.core :as interface.physics-2d :refer [get-wireframe-vectors]]
   [taoensso.timbre :as timbre]))

(defn- apply-event [scene event time]
  (timbre/debug ::event scene event)
  (try
    (let [events (loop.event/event->contexts scene event)]
      (when (empty? events)
        (timbre/error ::unhandled-event (str "Unhandled event: " event)))
      (reduce
       (fn [acc context]
         (-> [acc context]
             (loop.middleware/process time)
             (loop.handler/process time)
             (loop.reactor/process time)))
       scene
       events))
    (catch #?(:clj Throwable :cljs :default) e
      (timbre/error ::event-failed (str "Failed to process event " event) e)
      scene)))

(defn- threshold-reached [scene-key]
  ;; TODO add debugging info
  (timbre/error ::threshold-reached scene-key {})
  (throw (ex-info (str "Event threshold reached" scene-key) {:error ::threshold-reached})))

(defn- process-keyboard-events [{:keys [scene/key] :as scene} time]
  (let [keyboard-events @(state/get-input-events key)]
    (reset! (state/get-input-events key) [])
    (doseq [keyboard-event keyboard-events]
      (loop.keyboard/process scene keyboard-event time))
    (doseq [keyboard-down-key (state/get-down-keys)]
      (loop.keyboard/process scene {:input-event/key keyboard-down-key
                                    :input-event/type :key/while-down}
                             time))))

(defn run-scene [scene-atom delta current]
  (let [{scene-key :scene/key :as scene} @scene-atom
        events (state/get-scene-events scene-key)
        time {:time/delta delta :time/current current}]
    (process-keyboard-events scene time)
    (loop.ticker/process scene time)
    (loop [todo-events @events
           threshold 30]
      (when (zero? threshold)
        (threshold-reached scene-key))
      (reset! events [])
      (doseq [event todo-events]
        (vswap! scene-atom (fn [scene] (apply-event scene event time))))
      (when-not ^boolean (empty? @events)
        (recur @events (dec threshold))))))

(defn- remove-system
  ""
  [scene event]
  (when (#{:system/entity} (:remove/system-type event))
    (doseq [[_component-key component] (get-in scene (conj (event :remove/path) (:remove/key event) :entity/components))]
      ((:component/halt! component) (:component/state component))))
  (when (#{:system/component} (:remove/system-type event))
    (let [component (get-in scene (conj (event :remove/path) (:remove/key event)))]
      ((:component/halt! component) (:component/state component))))
  (update-in scene (:remove/path event) dissoc (:remove/key event)))

(defn handle-post-event [scene event]
  (condp = (:event/type event)
    :scene/start! (do (system.scene/start! (:scene/key event)) scene)
    :scene/halt! (do (system.scene/halt! (:scene/key event)) scene)
    :add/system (update-in scene (:add/path event) assoc (:add/key event) (:add/system event))
    :remove/system (remove-system scene event)))

(defn post-events [{:scene/keys [key] :as scene}]
  (let [events (state/get-scene-post-events key)
        events-todo @events]
    (reset! events [])
    (reduce handle-post-event scene events-todo)))

(defn debug-draw-wireframe [scene-key]
  (->>
    (get-wireframe-vectors state/physics-2d scene-key)
    (draw-wireframe state/graphics-2d scene-key)))

(defn run-scenes [delta time]
  (doseq [scene-key (keys (state/get-scenes))]
    (interface.physics-2d/step state/physics-2d scene-key delta)
    (run-scene (get-scene scene-key) delta time)
    (when @state/wireframe-enabled?
      (debug-draw-wireframe scene-key))
    (interface.graphics-2d/step state/graphics-2d scene-key)
    (vswap! (get-scene scene-key) post-events)))

(declare game-loop)

(def target-fps 60)

#?(:clj
   (def optimal-time (/ 1000000 target-fps)))

#?(:clj
   ;; TODO Actually implement this.
   (defn game-loop []
     (async/go-loop [delta 0
                     start-time (System/nanoTime)]
       (when (>= delta 1)
         (run-scenes delta start-time))
       (async/<! (async/timeout 0))
       (let [current-time (System/nanoTime)
             extra-d (if (>= delta 1) (dec delta) delta)
             new-delta (+ extra-d (/ (- current-time start-time) optimal-time))]
         (recur new-delta current-time)))))

#?(:cljs
   (defn
     game-loop
     ([] (fn [time] (game-loop time time)))
     ([old-time] (fn [time] (game-loop old-time time)))
     ([old-time time]
      (let [delta (- time old-time)]
        (run-scenes delta time))
      (.requestAnimationFrame js/window (game-loop time)))))

(defn start! []
  #?(:clj (game-loop)
     :cljs (.requestAnimationFrame js/window (game-loop))))
