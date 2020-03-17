(ns cardo.ticker.stats
  (:require
   [essen.core :as essen :refer [emit!]]
   [essen.system.ticker :as ticker]
   [integrant.core :as ig]))

(defmethod ig/init-key :ticker.stats/poisoned
  [_key {:ticker/keys [ticks damage]}]
  (let [remaining (atom ticks)
        last-time (atom (.now js/Date))
        poison-event {:event/damage damage
                      :event/damage-type :damage/poison}]
    (fn [context _delta time _state]
      (cond
        (zero? @remaining)
        (ticker/remove! context :ticker.stats/poisoned)
        (> (- time @last-time) 1000)
        (do (reset! last-time time)
            (swap! remaining dec)
            (emit! context :handler.stats/attack poison-event))))))
