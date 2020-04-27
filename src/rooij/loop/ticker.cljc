(ns rooij.loop.ticker
  (:require
   [integrant.core :as ig]))

(defn- subs-states [{:scene/keys [entities]}  {:ticker/keys [subs]}]
  (apply merge
         {}
         (for [[key components] subs
               [entity-key entity-value] (ig/find-derived entities key)]
           {entity-key (select-keys (:entity/state entity-value) components)})))

(defn process [{:scene/keys [key entities] :as scene} delta time]
  (doseq [[entity-key {:entity/keys [components state] :as entity}] entities
          [component-key {:component/keys [tickers]}] components
          [_ticker-key ticker-v] tickers]
    (let [context {:context/scene-key key
                   :context/entity-key entity-key
                   :context/component-key component-key
                   :context/delta delta
                   :context/time time
                   :context/subs (subs-states scene ticker-v)
                   :context/entity (:entity/state entity)}]
      ((:ticker/fn ticker-v) context (get state component-key)))))
