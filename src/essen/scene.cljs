(ns essen.scene
  (:require
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]))

(defn init-process [k opts]
  (-> opts
      (update :scene/entities vec->map :entity/key)
      (assoc :scene/key (last k))
      (->> (merge (ig/init-key k opts)))))
