(ns rooij.loop.ticker
  (:require
   [integrant.core :as ig]
   [rooij.system.entity :as entity]))

(defn- add-component
  [components acc component]
  (assoc acc component (get-in components [component :component/state])))

(defn- subs-states [entities subs]
  (apply merge
         (for [[key components] subs
               [derived-key opts] (ig/find-derived entities key)]
           (->> components
                (reduce (partial add-component (:entity/components opts)) {})
                (assoc {} derived-key)))))

(defn- get-subs [entity entities]
  (subs-states entities (-> entities entity :entity/subs)))

(defn process [{:scene/keys [key entities]} delta time]
  (doseq [[entity-key {:entity/keys [components] :as entity}] entities
          [component-key {:component/keys [tickers state]}] components
          [_ticker-key ticker-v] tickers]
    (let [context {:context/scene-key key
                   :context/entity-key entity-key
                   :context/component-key component-key
                   :context/delta delta
                   :context/time time
                   :context/subs (get-subs entity-key key)
                   :context/entity (entity/state entity)}]
      ((:ticker/fn ticker-v) context state))))
