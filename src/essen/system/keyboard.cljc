(ns essen.system.keyboard
  (:require
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]
   [essen.system.core :as system]))

(defmethod system/init-key :essen/keyboard [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts :keyboard/fn (ig/init-key k opts)))
