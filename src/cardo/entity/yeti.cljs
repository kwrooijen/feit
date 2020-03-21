(ns cardo.entity.yeti
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/yeti [_ opts]
  identity)

(def config
  {[:essen/entity :entity/monster :entity/yeti]

   {:entity/dynamic true
    :entity/components [(ig/ref :component/stats)]

    :component/stats
    {:stats/hp 60}}})
