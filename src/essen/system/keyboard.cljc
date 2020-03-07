(ns essen.system.keyboard
  (:require
   [integrant.core :as ig]
   [essen.system :as es]))

(defmethod es/init-key :essen/keyboard [k opts]
  (assoc opts :keyboard/fn (ig/init-key k opts)))
