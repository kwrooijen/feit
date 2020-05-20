(ns feit.system.keyboard
  (:require
   [feit.core.util :refer [->context map-kv top-key]]
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
      (as-> $ (assoc $ :keyboard/fn ((:keyboard/init $) keyboard-key $)))))

(defn preprocess-keyboards [scene-key entity-key component-key keyboard]
  (map-kv #(preprocess-keyboard (->context scene-key entity-key component-key) %1 %2) keyboard))

(defmethod system/init-key :feit/keyboard [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :keyboard/key (top-key k)
         :keyboard/init (system/get-init-key k {:required? true})
         :keyboard/fn nil))
