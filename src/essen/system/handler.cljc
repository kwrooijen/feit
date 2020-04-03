(ns essen.system.handler
  (:require
   [essen.system.core :as system]
   [integrant.core :as ig]
   [essen.util :refer [vec->map top-key]]))

(defmethod system/init-key :essen/handler [k opts]
  (-> opts
      (assoc :handler/key (or (:handler/route opts) (top-key k))
             :handler/fn (ig/init-key k opts))
      (update :handler/middleware vec->map :middleware/key)))
