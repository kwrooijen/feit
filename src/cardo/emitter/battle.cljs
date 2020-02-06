(ns cardo.emitter.battle
  (:require
   [integrant.core :as ig]))

(defn spacebar? [{:essen/keys [queue]}]
  (some (comp #{:key/spacebar} :event/key-down) queue))

(defmethod ig/init-key :emitter.battle/attack [_ _opts]
  (fn [state _this _time _delta]
    (when (spacebar? state)
      {:event :attack})))
