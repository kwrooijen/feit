(ns cardo.entity.skeleton
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/skeleton [_ opts]
  opts)

(def config
  {[:component/stats :component.skeleton/stats]
   {:stats/hp 23}

   [:component/equipment :component.skeleton/equipment] {}

   ^:dynamic
   [:essen/entity :entity/monster :entity/skeleton]
   {:entity/components
    [(ig/ref :component.skeleton/stats)
     (ig/ref :component.skeleton/equipment)]}})
