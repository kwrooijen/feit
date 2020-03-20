(ns cardo.entity.yeti
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :entity/yeti [_ opts]
  opts)

(def config
  {[:essen/entity :entity/monster :entity/yeti]

   {:entity/components [(ig/ref :component/stats)]
    :entity/dynamic true

    [:essen/component :component/stats]
    {:stats/hp 60}}})
