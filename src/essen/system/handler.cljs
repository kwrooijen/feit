(ns essen.system.handler
  (:require
   [essen.system :as es]
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]))

(defmethod es/init-key :essen/handler [k opts]
  (-> opts
      (assoc :handler/key (last k)
             :handler/fn (ig/init-key k opts))
      (update :handler/middleware vec->map :middleware/key)))
