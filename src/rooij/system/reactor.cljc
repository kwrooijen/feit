(ns rooij.system.reactor
  (:require
   [taoensso.timbre :as timbre]
   [rooij.util :refer [top-key]]
   [rooij.system.core :as system]
   [integrant.core :as ig]))

(defmethod system/init-key :rooij/reactor [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :reactor/key (top-key k)
         :reactor/init (get-method ig/init-key k)
         :reactor/fn (ig/init-key k opts)))
