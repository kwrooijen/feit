(ns rooij.system.handler
  (:require
   [taoensso.timbre :as timbre]
   [rooij.system.core :as system]
   [integrant.core :as ig]
   [rooij.util :refer [vec->map top-key]]))

(defmethod system/init-key :rooij/handler [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (assoc :handler/key (or (:handler/route opts) (top-key k))
             :handler/fn (ig/init-key k opts))
      (update :handler/middleware system/process-refs :middleware)))
