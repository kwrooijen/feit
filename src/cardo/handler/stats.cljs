(ns cardo.handler.stats
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :handler.stats/attack [_ {:context/keys [entity]}]
  (fn handler-stats--attack
    [_context
     {:event/keys [damage invincible?] :as _event}
     {:stats/keys [hp] :as state}
     _entity-state]
    (if invincible?
      (println entity "was attacked, but" entity "is invincible!")
      (println "Attacked for " damage))
    (assoc state :stats/hp (max 0 (- hp damage)))))
