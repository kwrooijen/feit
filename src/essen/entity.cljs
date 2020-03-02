(ns essen.entity
  (:require
   [integrant.core :as ig]
   [essen.state :refer [state]]
   [essen.util :refer [vec->map]]))

(defn- routes [{:entity/keys [components]}]
  (->>
   (for [{:component/keys [key handlers]} components
         k (keys handlers)]
     {k key})
   (apply merge)))

(defmethod ig/init-key :essen/entity [_ entity]
  entity)

(defmethod ig/init-key :essen/scene [_ entity]
  entity)

(defn- init-process-scene [k opts]
  (-> opts
      (update :scene/entities vec->map :entity/key)
      (assoc :scene/key (last k))
      (->> (merge (ig/init-key k opts)))))

(defn- init-process-entity [k opts]
  (-> opts
      (update :entity/components vec->map :component/key)
      (assoc :entity/routes (routes opts)
             :entity/key (last k))
      (->> (merge (ig/init-key k opts)))))

(defn- init-process-component [k opts]
  (-> opts
      (assoc :component/key (last k)
             :component/state (ig/init-key k opts))
      (update :component/tickers vec->map :ticker/key)
      (update :component/handlers vec->map :handler/key)))

(defn- init-process-handler [k opts]
  (assoc opts
         :handler/key (last k)
         :handler/fn (ig/init-key k opts)))

(defn- init-process-middleware [k opts]
  (assoc opts
         :middleware/key (last k)
         :middleware/fn (ig/init-key k opts)))

(defn- init-process-reactor [k opts]
  (assoc opts
         :reactor/key (last k)
         :reactor/fn (ig/init-key k opts)))

(defn- init-process-ticker [k opts]
  (assoc opts
         :ticker/key (last k)
         :ticker/fn (ig/init-key k opts)))

(defn- essen-init-key [k opts]
  (cond
    (ig/derived-from? k :essen/scene)
    (init-process-scene k opts)

    (ig/derived-from? k :essen/entity)
    (init-process-entity k opts)

    (ig/derived-from? k :essen/component)
    (init-process-component k opts)

    (ig/derived-from? k :essen/handler)
    (init-process-handler k opts)

    (ig/derived-from? k :essen/middleware)
    (init-process-middleware k opts)

    (ig/derived-from? k :essen/reactor)
    (init-process-reactor k opts)

    (ig/derived-from? k :essen/ticker)
    (init-process-ticker k opts)

    :else
    (ig/init-key k opts)))

(defn essen-init
  ([config] (essen-init config (keys config)))
  ([config keys]
   (ig/build config keys essen-init-key)))

(defn init-entity
  ([config entity] (init-entity config entity {}))
  ([config entity additions]
   (-> config
       (merge additions)
       (ig/prep [entity])
       (essen-init [entity])
       (get [:essen/entity entity]))))

(defn init-scene-system [config scene additions]
  (-> config
      (merge additions)
      (ig/prep [scene])
      (essen-init [scene])
      (get [:essen/scene scene])))

(defn init-scene
  ([config scene] (init-scene config scene {}))
  ([config scene additions]
   (let [system (init-scene-system config scene additions)]
     (swap! state assoc-in [:essen/scenes scene] (atom system))
     system)))

(defn get-scene [scene-key]
  (get-in @state [:essen/scenes scene-key]))
