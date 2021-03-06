(ns feit.system.reactor
  (:require [meta-merge.core :refer [meta-merge]]
            [feit.system.core :as system]
            [feit.core.util :refer [->context map-kv top-key]]
            [taoensso.timbre :as timbre]))

(defmethod system/init-key :feit/reactor [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :reactor/key (top-key k)
         :reactor/init (system/get-init-key k {:required? true})
         :reactor/fn nil))

(defn init [{:reactor/keys [key init] :as reactor}]
  (assoc reactor :reactor/fn (init key reactor)))

(defn preprocess-reactor [context reactor-key reactor-opts]
  (-> reactor-opts
      (->> (meta-merge (:reactor/ref reactor-opts)))
      (dissoc :reactor/ref)
      (system/merge-context context)
      (assoc :reactor/key reactor-key)
      (as-> $ (assoc $ :reactor/fn ((:reactor/init $) reactor-key $)))))

(defn preprocess-reactors [scene-key entity-key component-key reactor]
  (map-kv #(preprocess-reactor (->context scene-key entity-key component-key) %1 %2) reactor))
