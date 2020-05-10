(ns rooij.system.keyboard
  (:require
   [rooij.util :refer [->context map-kv]]
   [meta-merge.core :refer [meta-merge]]
   [rooij.system.core :as system]
   [taoensso.timbre :as timbre]))

(defmethod system/init-key :rooij/keyboard [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts :keyboard/init (system/get-init-key k)))

(defn init [{:keyboard/keys [key init] :as keyboard}]
  (assoc keyboard :keyboard/fn (init key keyboard)))

(defn preprocess-keyboard [context keyboard-key keyboard-opts]
  (-> keyboard-opts
      (->> (meta-merge (:keyboard/ref keyboard-opts)))
      (dissoc :keyboard/ref)
      (merge context)
      (assoc :keyboard/key keyboard-key)))

(defn preprocess-keyboards [scene-key keyboards]
  (map-kv #(preprocess-keyboard (->context scene-key %1) %1 %2) keyboards))
