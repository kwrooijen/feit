(ns rooij.system.keyboard
  (:require
   [rooij.system.core :as system]
   [taoensso.timbre :as timbre]))

(defmethod system/init-key :rooij/keyboard [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :keyboard/init (system/get-init-key k)
         :keyboard/fn nil))

(defn init [{:keyboard/keys [key init] :as keyboard}]
  (assoc keyboard :keyboard/fn (init key keyboard)))
