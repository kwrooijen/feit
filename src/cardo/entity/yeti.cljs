(ns cardo.entity.yeti
  (:require
   [integrant.core :as ig]))

(def config
  {[:component/stats :component.yeti/stats]
   {:stats/hp 60}

   ^:dynamic
   [:essen/entity :entity/monster :entity/yeti]
   {:entity/components
    [(ig/ref :component.yeti/stats)]}})
