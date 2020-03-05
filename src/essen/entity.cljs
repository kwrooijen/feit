(ns essen.entity
  (:require
   [integrant.core :as ig]
   [essen.state :refer [state persistent-entities]]
   [essen.util :refer [vec->map]]))

(defn path-state
  [entity component]
  [:scene/entities entity
   :entity/components (:component/key component)
   :component/state])

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
             :entity/key (last k)
             :entity/persistent (:persistent (meta k)))
      (->> (merge (ig/init-key k opts)))))

(defn- init-process-component [context k opts]
  (-> opts
      (assoc :component/key (last k)
             :component/state (ig/init-key k opts)
             :component/context (assoc context :context/component (last k)))
      (update :component/tickers vec->map :ticker/key)
      (update :component/handlers vec->map :handler/key)))

(defn- init-process-handler [k opts]
  (-> opts
      (assoc :handler/key (last k)
             :handler/fn (ig/init-key k opts))
      (update :handler/middleware vec->map :middleware/key)))

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

(defn- essen-init-key [context k opts]
  (cond
    (ig/derived-from? k :essen/scene)
    (init-process-scene k opts)

    (ig/derived-from? k :essen/entity)
    (init-process-entity k opts)

    (ig/derived-from? k :essen/component)
    (init-process-component context k opts)

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
  ([config context] (essen-init config context (keys config)))
  ([config context keys]
   (ig/build config keys (partial essen-init-key context))))

(defn persistent? [config entity]
  (-> config
      (find [:essen/entity entity])
      (first)
      (meta)
      :persistent))

(defn get-persistent-entity [config entity]
  (and (persistent? config entity)
       (get @persistent-entities entity)))

(defn init-entity
  ([config scene entity] (init-entity config scene entity {}))
  ([config scene entity additions]
   (or (get-persistent-entity config entity)
       (-> config
           (merge additions)
           (ig/prep [entity])
           (essen-init {:context/scene scene :context/entity entity} [entity])
           (get [:essen/entity entity])))))

(defn resolve-entity [config scene entity]
  (init-entity config scene (:key entity)))

(defn init-scene-entities [config scene]
  (update-in config [[:essen/scene scene] :scene/entities]
             (partial map (partial resolve-entity config scene))))

(defn init-scene-system [config scene additions]
  (-> config
      (init-scene-entities scene)
      (merge additions)
      (ig/prep [scene])
      (essen-init {:context/scene scene} [scene])
      (get [:essen/scene scene])))

;; TODO Add a way to group entities (possibly through deriving / hierarchy?)
;; TODO Add a way to have some sort of "entity creator". You don't want to use
;; ig/init-key everytime you spawn a bullet.
(defn init-scene
  ([config scene] (init-scene config scene {}))
  ([config scene additions]
   (let [system (init-scene-system config scene additions)]
     (swap! state assoc-in [:essen/scenes scene] (atom system))
     system)))
