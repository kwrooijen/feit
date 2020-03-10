(ns cardo.entity.yeti
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/yeti [_ opts]
  opts)

(def config
  {[:component/stats :component.yeti/stats]
   {:stats/hp 60}

   ^:dynamic
   [:essen/entity :entity/monster :entity/yeti]
   {:entity/components
    [(ig/ref :component.yeti/stats)]}})
