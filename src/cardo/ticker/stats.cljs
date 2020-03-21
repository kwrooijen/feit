(ns cardo.ticker.stats
  (:require
   [essen.core :as essen :refer [emit!]]
   [essen.system.ticker :as ticker]
   [integrant.core :as ig]))

(defmethod ig/init-key :ticker.stats/poisoned
  [_key {:ticker/keys [ticks damage] :context/keys [scene entity]}]
  (let [remaining (atom ticks)
        last-time (atom (.now js/Date))
        poison-event {:event/damage damage
                      :event/damage-type :damage/poison}]
    (fn ticker-stats--poisoned [_subs component {:tick/keys [time]} _state]
      (cond
        (zero? @remaining)
        (ticker/remove! scene entity component :ticker.stats/poisoned)
        (> (- time @last-time) 1000)
        (do (reset! last-time time)
            (swap! remaining dec)
            (emit! scene entity :handler.stats/attack poison-event))))))
