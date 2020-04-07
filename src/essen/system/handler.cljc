(ns essen.system.handler
  (:require
   [taoensso.timbre :as timbre]
   [essen.system.core :as system]
   [integrant.core :as ig]
   [essen.util :refer [vec->map top-key]]))

(defmethod system/init-key :essen/handler [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (assoc :handler/key (or (:handler/route opts) (top-key k))
             :handler/fn (ig/init-key k opts))
      (update :handler/middleware vec->map :middleware/key)))
