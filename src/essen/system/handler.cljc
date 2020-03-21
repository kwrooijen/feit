(ns essen.system.handler
  (:require
   [essen.system :as es]
   [integrant.core :as ig]
   [essen.util :refer [vec->map top-key]]))

(defmethod es/init-key :essen/handler [k opts]
  (-> opts
      (assoc :handler/key (top-key k)
             :handler/fn (ig/init-key k opts))
      (update :handler/middleware vec->map :middleware/key)))
