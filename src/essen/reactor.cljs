(ns essen.reactor
  (:require
   [integrant.core :as ig]))

(defn init-process [k opts]
  (assoc opts
         :reactor/key (last k)
         :reactor/fn (ig/init-key k opts)))
