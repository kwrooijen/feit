(ns rooij.system.ticker
  (:require [integrant-tools.core :as it]
            [meta-merge.core :refer [meta-merge]]
            [rooij.state :as state]
            [rooij.system.core :as system]
            [rooij.util :refer [->context map-kv top-key]]
            [taoensso.timbre :as timbre]))

(def context-keys
  [:context/scene-key
   :context/entity-key
   :context/component-key])

(defn path
  ([entity-key component-key]
   [:scene/entities entity-key
    :entity/components component-key
    :component/tickers])
  ([entity-key component-key ticker]
   [:scene/entities entity-key
    :entity/components component-key
    :component/tickers ticker]))

(defn preprocess-ticker [context ticker-key ticker-opts]
  (-> ticker-opts
      (->> (meta-merge (:ticker/ref ticker-opts)))
      (dissoc :ticker/ref)
      (->> (merge (select-keys context context-keys)))
      (assoc :ticker/key ticker-key)
      (as-> $ (assoc $ :ticker/fn ((:ticker/init $) ticker-key $)))))

(defn preprocess-tickers [scene-key entity-key component-key ticker]
  (map-kv #(preprocess-ticker (->context scene-key entity-key component-key) %1 %2) ticker))

(defn add!
  ([{:context/keys [scene-key entity-key component-key]} ticker]
   (add! scene-key entity-key component-key ticker {}))
  ([{:context/keys [scene-key entity-key component-key]} ticker opts]
   (add! scene-key entity-key component-key ticker opts))
  ([scene-key entity-key component-key ticker-key opts]
   (swap! (state/get-scene-post-events scene-key) conj
          {:add/path (path entity-key component-key)
           :add/key ticker-key
           :add/system (-> opts
                           (assoc :ticker/ref (it/find-derived-value @state/system ticker-key))
                           (->> (preprocess-ticker
                                 (->context scene-key entity-key component-key)
                                 ticker-key)))
           :event/type :add/system})))

(defn remove!
  ([{:context/keys [scene-key entity-key component-key]} ticker]
   (remove! scene-key entity-key component-key ticker))
  ([scene-key entity-key component-key ticker-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path (path entity-key component-key)
           :remove/key ticker-key
           :event/type :remove/system})))

(defmethod system/init-key :rooij/ticker [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :ticker/key (top-key k)
         :ticker/init (system/get-init-key k {:required? true})
         :ticker/fn nil))
