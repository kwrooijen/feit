(ns rooij.system.ticker
  (:require
   [meta-merge.core :refer [meta-merge]]
   [rooij.system.core :as system]
   [rooij.util :refer [->context map-kv top-key]]
   [taoensso.timbre :as timbre]))

(def context-keys
  [:context/scene-key
   :context/entity-key
   :context/component-key])

(defn preprocess-ticker [context ticker-key ticker-opts]
  (-> ticker-opts
      (->> (meta-merge (:ticker/ref ticker-opts)))
      (dissoc :ticker/ref)
      (->> (merge (select-keys context context-keys)))
      (assoc :ticker/key ticker-key)
      (as-> $ (assoc $ :ticker/fn ((:ticker/init $) ticker-key $)))))

(defn preprocess-tickers [scene-key entity-key component-key ticker]
  (map-kv #(preprocess-ticker (->context scene-key entity-key component-key) %1 %2) ticker))

(defmethod system/init-key :rooij/ticker [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :ticker/key (top-key k)
         :ticker/init (system/get-init-key k {:required? true})
         :ticker/fn nil))
