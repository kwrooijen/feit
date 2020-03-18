(ns cardo.handler.stats
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :handler.stats/attack [_ _opts]
  (fn [{:context/keys [entity _subs]}
       {:event/keys [damage invincible?] :as _event}
       {:stats/keys [hp] :as state}
       _entity-state]
    (if invincible?
      (println entity "was attacked, but" entity "is invincible!")
      (println "Attacked for " damage))
    (assoc state :stats/hp (max 0 (- hp damage)))))
