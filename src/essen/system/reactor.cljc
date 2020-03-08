(ns essen.system.reactor
  (:require
   [essen.system :as es]
   [integrant.core :as ig]))

(defmethod es/init-key :essen/reactor [k opts]
  (assoc opts
         :reactor/key (last k)
         :reactor/fn (ig/init-key k opts)))
