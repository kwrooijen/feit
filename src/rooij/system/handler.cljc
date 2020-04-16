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
             :reactor/init (system/get-init-key k)
             :reactor/fn nil)
      (update :handler/middleware system/process-refs :middleware)))

(defn init [{:handler/keys [key opts] :as handler}]
  (assoc handler :handler/fn (ig/init-key key opts)))
