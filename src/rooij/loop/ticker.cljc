(ns rooij.loop.ticker)

(defn process [{:scene/keys [key entities] :as scene} delta time]
  (doseq [[entity-key {:entity/keys [components state] :as entity}] entities
          [component-key {:component/keys [tickers]}] components
          [_ticker-key ticker-v] tickers]
    (let [context {:context/scene-key key
                   :context/entity-key entity-key
                   :context/component-key component-key
                   :context/delta delta
                   :context/time time
                   :context/entity (:entity/state entity)}]
      ((:ticker/fn ticker-v) context (get state component-key)))))
