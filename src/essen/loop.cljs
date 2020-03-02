(ns essen.loop
  (:require
   [essen.entity :as entity]
   [essen.state :refer [messages]]))

(defn- get-component [scene {:message/keys [entity route]}]
  (let [component-key (get-in scene [:scene/entities entity :entity/routes route])]
    (get-in scene [:scene/entities entity :entity/components component-key])))

(defn- get-middleware [component route]
  (get-in component [:component/handlers route :handler/middleware]))

;; TODO maybe we can use meta tags instead of this.
;; TODO ADD SCENE
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

(defn- run-reactors!
  [context event old-state new-state reactors]
  (doall
   (for [reactor reactors]
     ((:reactor/fn reactor) context event old-state new-state))))

(defn- update-scene [scene entity component handler context event]
  (update-in scene
             (entity/path-state entity component)
             (partial (:handler/fn handler) context event (:component/state component))))

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
      (run-reactors! context event old-state new-state (:component/reactors component)))
    new-scene))

(defn- apply-tickers [entities delta time]
  (doall
   (for [[entity-key {:entity/keys [components]}] entities
         [component-key {:component/keys [tickers state]}] components
         [_ticker-key ticker-v] tickers]
     (let [context {:context/entity entity-key
                    :context/component component-key}]
       ((:ticker/fn ticker-v) context delta time state)))))

(defn run [{:scene/keys [key entities] :as initial-scene} delta time]
  ;; TODO Add keyboard events BEFORE loop
  ;; HINT make separate message queue for keyboard
  (apply-tickers entities delta time)
  (loop [scene initial-scene
         todo-messages @(get @messages key)
         threshold 30]
    (if (zero? threshold)
      (do
        (println "THRESHOLD REACHED")
        :threshold-reached) ;; add debugging info
      (do
        (reset! (get @messages key) [])
        (let [new-scene (reduce apply-message scene todo-messages)
              new-messages @(get @messages key)]
          (if (empty? new-messages)
            new-scene
            (recur new-scene new-messages (dec threshold))))))))
