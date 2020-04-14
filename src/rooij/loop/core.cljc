(ns rooij.loop.core
  (:require
   #?(:clj [clojure.core.async :as async])
   [rooij.loop.event :as loop.event]
   [rooij.loop.handler :as loop.handler]
   [rooij.loop.key :as loop.key]
   [rooij.loop.middleware :as loop.middleware]
   [rooij.loop.reactor :as loop.reactor]
   [rooij.loop.ticker :as loop.ticker]
   [rooij.state :as state :refer [get-scene]]
   [rooij.interface.graphics-2d.core :as interface.graphics-2d]
   [rooij.interface.physics-2d.core :as interface.physics-2d]
   [taoensso.timbre :as timbre]))

(defn- apply-event [scene event]
  (timbre/debug ::event scene event)
  (reduce
   (fn [acc context]
     (-> [acc context]
         (loop.middleware/process)
         (loop.handler/process)
         (loop.reactor/process)))
   scene
   (loop.event/event->contexts scene event)))

(defn- threshold-reached [scene-key]
  ;; TODO add debugging info
  (timbre/error ::threshold-reached scene-key {})
  ::threshold-reached)

(defn run-scene [{:keys [scene/key] :as scene} delta time]
  (let [events (state/get-scene-events key)]
    (loop.key/process scene)
    (loop.ticker/process scene delta time)
    (loop [scene scene
           todo-events @events
           threshold 30]
      (if (zero? threshold)
        (threshold-reached key)
        (do
          (reset! events [])
          (let [new-scene (reduce apply-event scene todo-events)]
            (if ^boolean (empty? @events)
              new-scene
              (recur new-scene @events (dec threshold)))))))))

(defn run [scene-key delta time]
  (swap! (get-scene scene-key) run-scene delta time))

;; TODO find a proper solution for this
(def physics (atom nil))

(defn add! [p]
  (reset! physics p))

(defn run-scenes [delta time]
  (doseq [scene-key (keys (state/get-scenes))]
    (swap! (get-scene scene-key) run-scene delta time)
    (state/physics-2d delta scene-key)
    (interface.graphics-2d/step state/graphics-2d scene-key)))

(declare game-loop)

(def target-fps 60)

#?(:clj
   (def optimal-time (/ 1000000 target-fps)))

#?(:clj
   (defn game-loop []
     (async/go-loop [delta 0
                     start-time (System/nanoTime)]
       (when (>= delta 1)
         (run-scenes delta start-time))
       (async/<! (async/timeout 0))
       (let [current-time (System/nanoTime)
             extra-d (if (>= delta 1) (dec delta) delta)
             new-delta (+ extra-d (/ (- current-time start-time) optimal-time) )]
         (recur new-delta current-time)))))

#?(:cljs
   (defn
     game-loop
     ([] (fn [time] (game-loop time time)))
     ([old-time] (fn [time] (game-loop old-time time)))
     ([old-time time]
      (let [delta  (/ (- time old-time) 1000)]
        (run-scenes delta time))
      (.requestAnimationFrame js/window (game-loop time)))))

(defn start! []
  #?(:clj (game-loop)
     :cljs (.requestAnimationFrame js/window (game-loop))))
