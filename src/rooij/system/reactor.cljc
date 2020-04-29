(ns rooij.system.reactor
  (:require
   [taoensso.timbre :as timbre]
   [rooij.util :refer [top-key]]
   [rooij.system.core :as system]))

(defmethod system/init-key :rooij/reactor [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :reactor/key (top-key k)
         :reactor/init (system/get-init-key k)
         :reactor/fn nil))

(defn init [{:reactor/keys [key init] :as reactor}]
  (assoc reactor :reactor/fn (init key reactor)))
