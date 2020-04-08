(ns rooij.system.keyboard
  (:require
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]
   [rooij.system.core :as system]))

(defmethod system/init-key :rooij/keyboard [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts :keyboard/fn (ig/init-key k opts)))
