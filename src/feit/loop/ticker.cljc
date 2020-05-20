(ns feit.loop.ticker)

(defn process [{:scene/keys [entities]} time]
  ;; TODO Preprocessing all tickers would probably boost performanceh here
  (doseq [[_entity-key {:entity/keys [components state]}] entities
          [component-key {:component/keys [tickers]}] components
          [_ticker-key ticker-v] tickers]
    ((:ticker/fn ticker-v) time (get state component-key))))
