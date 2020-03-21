(ns cardo.component.equipment
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :component/equipment [_ _opts]
  {:equipment/armor :cool})

(def config
  {[:essen/component :component/equipment] {}})
