(ns essen.loop.core
  (:require
   [essen.module.pixi.render]
   [clojure.core.async :as async]
   [essen.loop.event :as loop.event]
   [essen.loop.middleware :as loop.middleware]
   [essen.loop.handler :as loop.handler]
   [essen.loop.reactor :as loop.reactor]
   [essen.loop.ticker :as loop.ticker]
   [essen.loop.key :as loop.key]
   [essen.state :as state :refer [get-scene]]))

(defn- apply-event [scene event]
  (reduce
   (fn [acc context]
     (-> [acc context]
         (loop.middleware/process)
         (loop.handler/process)
         (loop.reactor/process)))
   scene
   (loop.event/event->contexts scene event)))

(defn- threshold-reached [_key]
  (println "THRESHOLD REACHED")
  ;; TODO add debugging info
  :threshold-reached)

(defn run-scene [{:keys [scene/key] :as scene} delta time]
  (let [events (get @state/events key)]
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

(defn get-current-time []
  #?(:clj (System/nanoTime)
     :cljs (.now js/Date)))

;; TODO find a proper solution for this
(def physics (atom nil))

(defn add! [p]
  (reset! physics p))

(defn run-scenes [delta time]
  (doseq [scene-key (keys (:essen/scenes @state/state))]
    (swap! (get-scene scene-key) run-scene delta time)
    (essen.module.pixi.render/render scene-key)
    (when @physics
      (@physics))))

(declare game-loop)

(def target-fps 60)

#?(:clj
   (def optimal-time (/ 1000000 target-fps)))

#?(:clj
   (defn game-loop []
     (async/go-loop [delta 0
                     start-time (get-current-time)]
       (when (>= delta 1)
         (run-scenes delta time))
       (async/<! (async/timeout 0))
       (let [current-time (get-current-time)
             extra-d (if (>= delta 1) (dec delta) delta)
             new-delta (+ extra-d (/ (- current-time start-time) optimal-time) )]
         (recur new-delta current-time)))))

#?(:cljs
   (defn
     game-loop
     ([old-time] (fn [time] (game-loop old-time time)))
     ([old-time time]
      (let [delta  (/ (- time old-time) 1000)]
        (run-scenes delta time))
      (.requestAnimationFrame js/window (game-loop time)))))

(defn start! []
  #?(:clj (game-loop)
     :cljs (.requestAnimationFrame js/window (game-loop (get-current-time)))))
