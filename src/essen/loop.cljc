(ns essen.loop
  (:require
   [integrant.core :as ig]
   [essen.system.entity :as entity]
   [essen.state :as state :refer [get-scene persistent-entities input-messages]]
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

(defn- add-context-subs [component entity entities]
  (assoc-in component [:component/context :context/subs]
            (subs-states entities (-> entities entity :entity/subs))))

(defn- add-context [component entity {:scene/keys [entities] :as scene}]
  (-> component
      (add-context-subs entity entities)
      ;; TODO OPTIMIZE Post init do a walk to add contexts to components.
      (update :component/context assoc
              :context/entity    entity
              :context/scene     (:scene/key scene)
              :context/component (:component/key component))))

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

(defn- save-entity! [scene entity-key]
  (let [entity (get-in scene [:scene/entities entity-key])]
    (when (:entity/persistent entity)
      (swap! persistent-entities assoc entity-key entity))))

(defn- apply-message [scene {:message/keys [entity route content] :as message}]
  (let [component (-> (get-component scene message)
                      (add-context entity scene))
        old-state (:component/state component)
        event (preprocess-event component route content)
        handler (get-in component [:component/handlers route])
        new-scene (update-scene scene entity component handler event)
        new-state (:component/state (get-component new-scene message))]
    (when-not (identical? old-state new-state)
      (apply-reactors! component event old-state new-state)
      (save-entity! new-scene entity))
    new-scene))

(defn- apply-tickers [{:scene/keys [key entities]} delta time]
  (doseq [[entity-key {:entity/keys [components]}] entities
          [component-key {:component/keys [tickers state]}] components
          [_ticker-key ticker-v] tickers]
    (let [context {:context/scene key
                   :context/entity entity-key
                   :context/component component-key}]
      ((:ticker/fn ticker-v) context delta time state))))

(defn keyboard-context [{:scene/keys [key entities]} {:keyboard/keys [subs]}]
  {:context/scene key
   :context/subs (subs-states entities subs)})

(defn- apply-key-event [scene keyboard {:input-message/keys [tag]}]
  (let [keyboard (get keyboard tag)]
    (when-let [f (:keyboard/fn keyboard)]
      (f (keyboard-context scene keyboard)))))

(defn- apply-key-events [{:scene/keys [key keyboard] :as scene}]
  (swap! (get @input-messages key)
         (fn [events]
           (doall (map (partial apply-key-event scene keyboard) events))
           [])))

(defn- threshold-reached [key]
  (println "THRESHOLD REACHED")
  ;; TODO add debugging info
  :threshold-reached)

(defn run [scene-key delta time]
  (swap! (get-scene scene-key)
         (fn [scene]
           (let [messages (get @state/messages scene-key)]
             (apply-key-events scene)
             (apply-tickers scene delta time)
             (loop [scene scene
                    todo-messages @messages
                    threshold 30]
               (if (zero? threshold)
                 (threshold-reached scene-key)
                 (do
                   (reset! messages [])
                   (let [new-scene (reduce apply-message scene todo-messages)]
                     (if (empty? @messages)
                       new-scene
                       (recur new-scene @messages (dec threshold)))))))))))
