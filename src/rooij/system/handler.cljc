(ns rooij.system.handler
  (:require
   [integrant.core :as ig]
   [rooij.system.core :as system]
   [rooij.util :refer [top-key]]
   [taoensso.timbre :as timbre]))

(defmethod system/init-key :rooij/handler [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :handler/key (or (:handler/route opts) (top-key k))
         :handler/init (system/get-init-key k)
         :handler/fn nil))

(defn init [{:handler/keys [key opts] :as handler}]
  (assoc handler :handler/fn (ig/init-key key opts)))
