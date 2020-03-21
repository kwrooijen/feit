(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [essen.state :as state :refer [input-messages messages game systems]]
   [essen.util :refer [vec->map spy]]
   [essen.system]
   [essen.system.component]
   [essen.system.handler]
   [essen.system.keyboard]
   [essen.system.middleware]
   [essen.system.reactor]
   [essen.system.scene]
   [essen.system.ticker]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [integrant.core :as ig]
   [essen.render]
   [spec-signature.core :refer-macros [sdef]]
   [essen.module.pixi.render :as rr]))

(defn- entity-keys
  "Get all top level keys of every entity system"
  [config]
  (->> :essen/entity
       (it/find-derived-values config)
       (map keys)
       (apply concat)))

(defn- derive-composite-all
  "Globally composite-derive all keys in `config` that derive from `key`"
  [config]
  (doseq [k (keys config)]
    (when (coll? k) (it/derive-composite k)))

  (doseq [k (entity-keys config)]
    (when (coll? k)
      (it/derive-composite k))))

(def ^:private default-keys
  {:context/entity (ig/ref :context/entity)
   :context/scene (ig/ref :context/scene)
   :scene/opts (ig/ref :scene/opts)})

(defn add-context
  "Add a refence to context for all keys. This is necessary so that components
  know which entity / scene they belong to."
  [acc k v]
  (if (and (map? v))
    (assoc acc k (merge v default-keys))
    (assoc acc k v)))

(defn add-context-to-entities [config]
  (->> (for [[entity-key entity-value] (ig/find-derived config :essen/entity)]
         [entity-key (reduce-kv add-context {} entity-value)])
       (into {})
       (merge config)))

(defn prep [config]
  (->> config
       (add-context-to-entities)
       (reduce-kv add-context {})
       (ig/prep)))

(defn- start-physics [config]
  (-> config
      (ig/prep [:matterjs/start])
      (ig/init [:matterjs/start])
      (it/find-derived-value :matterjs/start)
      (rr/add-ticker :essen/physics)))

(defn setup [{:keys [:essen/config :essen.module/render] :as game-config}]
  ((:essen/setup render) config)
  (derive-composite-all config)
  (reset! game (update game-config :essen/config prep))
  (start-physics config))

(defn emit!
  ([{:context/keys [scene entity]} route content]
   (emit! scene entity route content))
  ([scene entity route content]
   (swap! (get @messages scene)
          conj {:message/entity entity
                :message/route route
                :message/content content})))

(defn scenes []
  (set (keys (:essen/scenes @state/state))))

;; TODO Maybe use Specter to clean this logic up
(defn entities
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  [scene-key entity-key]
  (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
       (map (fn [[k v]] [k (:entity/components v)]))
       (map (fn [[k v]] [k (into {} (map (fn [[kk vv]] [kk (:component/state vv)]) v))]))
       (into {})))

(defn entity [scene-key entity-key]
  (get (entities scene-key entity-key) entity-key))

