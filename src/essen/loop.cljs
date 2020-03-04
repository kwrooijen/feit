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

;; TODO ~maybe we can use meta tags instead of this.~
;; Probably to pre-process this, and put it in :component/context key
(defn- get-context [scene component message]
  {:context/scene scene
   :context/entity (:message/entity message)
   :context/component (:component/key component)})

(defn- apply-middleware
  [context state event [_ middleware]]
  ((:middleware/fn middleware) context event state))

(defn- preprocess-event
  [middleware context state content]
  (reduce (partial apply-middleware context state) content middleware))

(defn- apply-reactors!
  [context event old-state new-state reactors]
  (doseq [reactor reactors]
    ((:reactor/fn reactor) context event old-state new-state)))

(defn- update-scene
  [scene entity {:component/keys [state] :as component} handler context event]
  (update-in scene
             (entity/path-state entity component)
             (partial (:handler/fn handler) context event state)))

(defn- apply-message [scene {:message/keys [entity route content] :as message}]
  (let [component (get-component scene message)
        context (get-context (:scene/key scene) component message)
        middleware (get-middleware component route)
        old-state (:component/state component)
        event (preprocess-event middleware context old-state content)
        handler (get-in component [:component/handlers route])
        new-scene (update-scene scene entity component handler context event)
        new-state (:component/state (get-component new-scene message))]
    (when-not (identical? old-state new-state)
      (apply-reactors! context event old-state new-state (:component/reactors component)))
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

(defn run [{:scene/keys [key] :as initial-scene} delta time]
  ;; TODO Add keyboard events BEFORE loop
  ;; HINT make separate message queue for keyboard
  ;; TODO @(get @state/messages key) only needs to be dereffed once (performance)
  (apply-tickers initial-scene delta time)
  (loop [scene @(get-scene key)
         todo-messages @(get @state/messages key)
         threshold 30]
    (if (zero? threshold)
      (threshold-reached key)
      (do
        (reset! (get @state/messages key) [])
        (let [new-scene (reduce apply-message scene todo-messages)
              new-messages @(get @state/messages key)]
          (if (empty? new-messages)
            new-scene
            (recur new-scene new-messages (dec threshold))))))))
