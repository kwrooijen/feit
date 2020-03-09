(ns cardo.middleware.stats
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :middleware.stats/invincible [_ _opts]
  (fn [_context event _state]
    (assoc event
           :event/damage 0
           :event/invincible? true)))
