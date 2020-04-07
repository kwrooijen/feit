(ns essen.system.reactor
  (:require
   [taoensso.timbre :as timbre]
   [essen.util :refer [top-key]]
   [essen.system.core :as system]
   [integrant.core :as ig]))

(defmethod system/init-key :essen/reactor [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :reactor/key (top-key k)
         :reactor/init (get-method ig/init-key k)
         :reactor/fn (ig/init-key k opts)))
