(ns essen.loop
  (:require
   [essen.entity :as entity]
   [essen.state :as state :refer [get-scene]]
   [essen.component :as component]))

(defn- get-component [scene {:message/keys [entity route]}]
  (->> (get-in scene [:scene/entities entity :entity/routes route])
       (component/path entity)
       (get-in scene)))

(defn- get-middleware [component route]
  (get-in component [:component/handlers route :handler/middleware]))

(defn- apply-middleware
  [context state event [_ middleware]]
  ((:middleware/fn middleware) context event state))

(defn- preprocess-event
  [{:component/keys [context state] :as component} route content]
  (reduce (partial apply-middleware context state)
          content
          (get-middleware component route)))

(defn- apply-reactors!
  [{:component/keys [reactors context]} event old-state new-state]
  (doseq [reactor reactors]
    ((:reactor/fn reactor) context event old-state new-state)))

(defn- update-scene
  [scene entity {:component/keys [state context] :as component} handler event]
  (update-in scene
             (entity/path-state entity component)
             (partial (:handler/fn handler) context event state)))

(defn- apply-message [scene {:message/keys [entity route content] :as message}]
  (let [component (get-component scene message)
        old-state (:component/state component)
        event (preprocess-event component route content)
        handler (get-in component [:component/handlers route])
        new-scene (update-scene scene entity component handler event)
        new-state (:component/state (get-component new-scene message))]
    (when-not (identical? old-state new-state)
      (apply-reactors! component event old-state new-state))
    new-scene))

(defn- apply-tickers [{:scene/keys [key entities]} delta time]
  (doseq [[entity-key {:entity/keys [components]}] entities
          [component-key {:component/keys [tickers state]}] components
          [_ticker-key ticker-v] tickers]
    (let [context {:context/scene key
                   :context/entity entity-key
                   :context/component component-key}]
      ((:ticker/fn ticker-v) context delta time state))))

(defn threshold-reached [key]
  (println "THRESHOLD REACHED")
  ;; TODO add debugging info
  :threshold-reached)

(defn run [key delta time]
  ;; TODO Add keyboard events BEFORE loop
  ;; HINT make separate message queue for keyboard
  (let [scene (get-scene key)
        messages (get @state/messages key)]
    (apply-tickers @scene delta time)
    (loop [scene @scene
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
