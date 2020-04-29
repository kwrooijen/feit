(ns rooij.system.handler
  (:require
   [rooij.system.core :as system]
   [taoensso.timbre :as timbre]))

(defmethod system/init-key :rooij/handler [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :handler/init (system/get-init-key k)
         :handler/fn nil))

(defn init [{:handler/keys [key init] :as handler}]
  (assoc handler :handler/fn (init key handler)))
