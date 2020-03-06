(ns essen.system.scene
  (:require
   [essen.system :as es]
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]))

(defmethod es/init-key :essen/scene [k opts]
  (-> opts
      (update :scene/entities vec->map :entity/key)
      (assoc :scene/key (last k))
      (->> (merge (ig/init-key k opts)))))
