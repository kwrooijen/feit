(ns essen.handler
  (:require
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]))

(defn init-process [k opts]
  (-> opts
      (assoc :handler/key (last k)
             :handler/fn (ig/init-key k opts))
      (update :handler/middleware vec->map :middleware/key)))
