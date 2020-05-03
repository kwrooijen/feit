(ns rooij.loop.core
  (:require
   #?(:clj [clojure.core.async :as async])
   [rooij.loop.event :as loop.event]
   [rooij.loop.handler :as loop.handler]
   [rooij.loop.keyboard :as loop.keyboard]
   [rooij.loop.middleware :as loop.middleware]
   [rooij.loop.reactor :as loop.reactor]
   [rooij.loop.ticker :as loop.ticker]
   [rooij.system.scene :as system.scene]
   [rooij.state :as state :refer [get-scene]]
   [rooij.interface.graphics-2d.core :as interface.graphics-2d :refer [draw-wireframe]]
   [rooij.interface.physics-2d.core :as interface.physics-2d :refer [get-wireframe-vectors]]
   [taoensso.timbre :as timbre]))

(defn- apply-event [scene event]
  (timbre/debug ::event scene event)
  (try
    (reduce
     (fn [acc context]
       (-> [acc context]
           (loop.middleware/process)
           (loop.handler/process)
           (loop.reactor/process)))
     scene
     (loop.event/event->contexts scene event))
    (catch #?(:clj Throwable :cljs :default) e
      (timbre/error ::event-failed (str "Failed to process event " event) e)
      scene)))

(defn- threshold-reached [scene-key]
  ;; TODO add debugging info
  (timbre/error ::threshold-reached scene-key {})
  (throw (ex-info (str "Event threshold reached" scene-key) {:error ::threshold-reached})))

(defn- process-keyboard-events [{:keys [scene/key] :as scene}]
  (let [keyboard-events @(state/get-input-events key)
        context {:context/scene-key key
                 :context/scene scene}]
    (reset! (state/get-input-events key) [])
    (doseq [keyboard-event keyboard-events]
      (loop.keyboard/process context keyboard-event))
    (doseq [keyboard-down-key (state/get-down-keys)]
      (loop.keyboard/process context {:input-event/key keyboard-down-key
                                      :input-event/type :key/while-down}))))

(defn run-scene [scene-atom delta time]
  (let [{scene-key :scene/key :as scene} @scene-atom
        events (state/get-scene-events scene-key)]
    (process-keyboard-events scene)
    (loop.ticker/process scene delta time)
    (loop [todo-events @events
           threshold 30]
      (when (zero? threshold)
        (threshold-reached scene-key))
      (reset! events [])
      (doseq [event todo-events]
        (swap! scene-atom (fn [scene] (apply-event scene event))))
      (when-not ^boolean (empty? @events)
        (recur @events (dec threshold))))))

(defn handle-post-event [scene event]
  (condp = (:event/type event)
    :scene/start! (do (system.scene/start! (:scene/key event)) scene)
    :scene/halt! (do (system.scene/halt! (:scene/key event)) scene)
    :add/system (update-in scene (:add/path event) assoc (:add/key event) (:add/system event))
    :remove/system (update-in scene (:remove/path event) dissoc (:remove/key event))))

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
    (debug-draw-wireframe scene-key)
    (interface.graphics-2d/step state/graphics-2d scene-key)
    (swap! (get-scene scene-key) post-events)))

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
