(ns cardo.entity.skeleton
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/skeleton [_ opts]
  identity)

(def config
  {[:essen/entity :entity/monster :entity/skeleton]
   {:entity/dynamic true
    :entity/components
    [(ig/ref :component/stats)
     (ig/ref :component/equipment)]

    [:essen/component :component/stats]
    {:stats/hp 23}

    [:essen/component :component/equipment] {}}})
