(ns rooij.system.keyboard
  (:require
   [rooij.system.core :as system]
   [rooij.util :refer [top-key]]
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]))

(defmethod system/init-key :rooij/keyboard [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :keyboard/key (top-key k)
         :keyboard/init (system/get-init-key k)
         :keyboard/fn nil))

(defn init [{:keyboard/keys [key opts] :as keyboard}]
  (assoc keyboard :keyboard/fn (ig/init-key key opts)))
