(ns essen.system.keyboard
  (:require
   [integrant.core :as ig]
   [essen.system.core :as system]))

(defmethod system/init-key :essen/keyboard [k opts]
  (assoc opts :keyboard/fn (ig/init-key k opts)))
