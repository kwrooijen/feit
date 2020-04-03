(ns essen.loop.core
  (:require
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
