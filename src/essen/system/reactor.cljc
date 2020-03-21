(ns essen.system.reactor
  (:require
   [essen.util :refer [top-key]]
   [essen.system :as es]
   [integrant.core :as ig]))

(defmethod es/init-key :essen/reactor [k opts]
  (assoc opts
         :reactor/key (top-key k)
         :reactor/fn (ig/init-key k opts)))
