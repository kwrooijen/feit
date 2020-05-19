(ns feit.system.keyboard
  (:require
   [feit.util :refer [->context map-kv]]
   [meta-merge.core :refer [meta-merge]]
   [feit.system.core :as system]
   [taoensso.timbre :as timbre]))

(defmethod system/init-key :feit/keyboard [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts :keyboard/init (system/get-init-key k)))

(defn preprocess-keyboard [context keyboard-key keyboard-opts]
  (-> keyboard-opts
      (->> (meta-merge (:keyboard/ref keyboard-opts)))
      (dissoc :keyboard/ref)
      (merge context)
      (assoc :keyboard/key keyboard-key)
      (as-> $ (assoc $ :keyboard/fn ((:keyboard/init $) keyboard-key $)))))

(defn preprocess-keyboards [scene-key keyboards]
  (map-kv #(preprocess-keyboard (->context scene-key) %1 %2) keyboards))
