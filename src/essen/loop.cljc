(ns essen.loop
  (:require
   [integrant.core :as ig]
   [essen.system.entity :as entity]
   [essen.state :as state :refer
    [get-scene
     persistent-components
     input-messages]]
   [essen.system.component :as component]))

(defn- add-component
  [components acc component]
  (assoc acc component (get-in components [component :component/state])))

(defn- subs-states [entities subs]
  (apply merge
         (for [[key components] subs
               [derived-key opts] (ig/find-derived entities key)]
           (->> components
                (reduce (partial add-component (:entity/components opts)) {})
                (assoc {} derived-key)))))

(defn- get-subs [entity entities]
  (subs-states entities (-> entities entity :entity/subs)))

(defn- get-component [scene {:message/keys [entity handler]}]
  (->> (get-in scene [:scene/entities entity :entity/handlers handler])
       (component/path entity)
       (get-in scene)))

(defn- get-middleware [component handler]
  (get-in component [:component/handlers handler :handler/middleware]))

(defn- apply-middleware
  [subs state event [_ middleware] entity-state]
  ((:middleware/fn middleware) subs event state entity-state))

(defn- preprocess-event
  [{:component/keys [state] :as component} subs handler content entity-state]
  (reduce (partial apply-middleware subs state entity-state)
          content
          (get-middleware component handler)))

(defn- apply-reactors!
  [{:component/keys [reactors]} subs event old-state new-state entity-state]
  (doseq [[_k reactor] reactors]
    ((:reactor/fn reactor) subs event old-state new-state entity-state)))

(defn- update-scene
  [scene entity component handler subs event entity-state]
  (update-in scene
             (entity/path-state entity component)
             #((:handler/fn handler) subs event % entity-state)))

(defn- save-component! [scene entity-key component-key]
  (let [component (get-in scene (component/path entity-key component-key))]
    (when (:component/persistent component)
      (swap! persistent-components assoc [entity-key component-key] component))))

(defn- apply-message [scene {:message/keys [entity handler content] :as message}]
  (let [component (-> (get-component scene message))
        subs (get-subs entity (:scene/entities scene))
        entity-state (entity/state (-> scene :scene/entities entity))
        old-state (:component/state component)
        event (preprocess-event component subs handler content entity-state)
        handler (get-in component [:component/handlers handler])
        new-scene (update-scene scene entity component handler subs event entity-state)
        new-state (:component/state (get-component new-scene message))
        new-entity-state (entity/state (-> new-scene :scene/entities entity))]
    (when-not (identical? old-state new-state)
      (apply-reactors! component subs event old-state new-state new-entity-state)
      (save-component! new-scene entity (:component/key component)))
    new-scene))

(defn- apply-tickers [{:scene/keys [key entities]} delta time]
  (doseq [[entity-key {:entity/keys [components] :as entity}] entities
          [component-key {:component/keys [tickers state]}] components
          [_ticker-key ticker-v] tickers]
    (let [subs (get-subs entity-key key)
          tick {:tick/delta delta :tick/time time}
          entity-state (entity/state entity)]
      ((:ticker/fn ticker-v) subs component-key tick state entity-state))))

(defn- apply-key-event
  [{:scene/keys [entities]}
   keyboard
   {:input-message/keys [tag]}]
  (let [{:keyboard/keys [subs] :as keyboard} (get keyboard tag)]
    (when-let [f (:keyboard/fn keyboard)]
      (f (subs-states entities subs)))))

(defn- apply-key-events [{:scene/keys [key keyboard] :as scene}]
  (swap! (get @input-messages key)
         (fn [events]
           (doseq [event events]
             (apply-key-event scene keyboard event))
           [])))

(defn- threshold-reached [key]
  (println "THRESHOLD REACHED")
  ;; TODO add debugging info
  :threshold-reached)

(defn run-scene [{:keys [scene/key] :as scene} delta time]
  (let [messages (get @state/messages key)]
    (apply-key-events scene)
    (apply-tickers scene delta time)
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
