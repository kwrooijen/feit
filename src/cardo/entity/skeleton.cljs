(ns cardo.entity.skeleton
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/skeleton [_ opts]
  opts)

(def config
  {[:essen/entity :entity/monster :entity/skeleton]
   {:entity/components
    [(ig/ref :component/stats)
     (ig/ref :component/equipment)]

    :entity/dynamic true

    [:essen/component :component/stats]
    {:stats/hp 23}

    [:essen/component :component/equipment] {}}})
