(ns feit.system.ticker
  (:require
   [meta-merge.core :refer [meta-merge]]
   [feit.system.core :as system]
   [feit.core.util :refer [->context map-kv top-key]]
   [taoensso.timbre :as timbre]))

(defn preprocess-ticker [context ticker-key ticker-opts]
  (-> ticker-opts
      (->> (meta-merge (:ticker/ref ticker-opts)))
      (dissoc :ticker/ref)
      (system/merge-context context)
      (assoc :ticker/key ticker-key)
      (as-> $ (assoc $ :ticker/fn ((:ticker/init $) ticker-key $)))))

(defn preprocess-tickers [scene-key entity-key component-key ticker]
  (map-kv #(preprocess-ticker (->context scene-key entity-key component-key) %1 %2) ticker))

(defmethod system/init-key :feit/ticker [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :ticker/key (top-key k)
         :ticker/init (system/get-init-key k {:required? true})
         :ticker/fn nil))
