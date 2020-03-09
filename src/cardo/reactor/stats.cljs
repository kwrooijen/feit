(ns cardo.reactor.stats
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :reactor.stats/dead? [_ _opts]
  (fn [_context _event old-state state]
    (when (and (not (zero? (:stats/hp old-state)))
               (zero? (:stats/hp state)))
      ;; (essen/emit! k :handler.sprite/animation-dead)
      (println "YOU DIED"))
    state))
