(ns essen.loop
  (:require
   [essen.loop.event :as loop.event]
   [essen.loop.middleware :as loop.middleware]
   [essen.loop.handler :as loop.handler]
   [essen.loop.reactor :as loop.reactor]
   [essen.loop.ticker :as loop.ticker]
   [essen.loop.key :as loop.key]
   [essen.state :as state :refer [get-scene]]))

(defn- apply-message [scene message]
  (-> (loop.event/event->context scene message)
      (loop.middleware/process)
      (loop.handler/process)
      (loop.reactor/process)
      (get :context/scene)))

(defn- threshold-reached [_key]
  (println "THRESHOLD REACHED")
  ;; TODO add debugging info
  :threshold-reached)

(defn run-scene [{:keys [scene/key] :as scene} delta time]
  (let [messages (get @state/messages key)]
    (loop.key/process scene)
    (loop.ticker/process scene delta time)
    (loop [scene scene
           todo-messages @messages
           threshold 30]
      (if (zero? threshold)
        (threshold-reached key)
        (do
          (reset! messages [])
          (let [new-scene (reduce apply-message scene todo-messages)]
            (if (empty? @messages)
              new-scene
              (recur new-scene @messages (dec threshold)))))))))

(defn run [scene-key delta time]
  (swap! (get-scene scene-key) run-scene delta time))
