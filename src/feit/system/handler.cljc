(ns feit.system.handler
  (:require [meta-merge.core :refer [meta-merge]]
            [feit.system.core :as system]
            [feit.util :refer [->context map-kv]]
            [taoensso.timbre :as timbre]))

(defmethod system/init-key :feit/handler [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :handler/init (system/get-init-key k {:required? true})
         :handler/fn nil))

(defn preprocess-handler [context handler-key handler-opts]
  (-> handler-opts
      (->> (meta-merge (:handler/ref handler-opts)))
      (dissoc :handler/ref)
      (merge context)
      (assoc :handler/key handler-key)
      (as-> $ (assoc $ :handler/fn ((:handler/init $) handler-key $)))))

(defn preprocess-handlers [scene-key entity-key component-key handlers]
  (map-kv #(preprocess-handler (->context scene-key entity-key component-key) %1 %2) handlers))
