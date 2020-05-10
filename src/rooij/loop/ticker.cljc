(ns rooij.loop.ticker)

(defn process [{:scene/keys [entities]} delta time]
  (let [context {:context/delta delta
                 :context/time time}]
    (doseq [[_entity-key {:entity/keys [components state]}] entities
            [component-key {:component/keys [tickers]}] components
            [_ticker-key ticker-v] tickers]
      ((:ticker/fn ticker-v) context (get state component-key)))))
