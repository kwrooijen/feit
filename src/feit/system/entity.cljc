(ns feit.system.entity
  (:require
   [com.rpl.specter :as sp :refer [ALL MAP-KEYS MAP-VALS]]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [feit.system.component :refer [preprocess-components]]
   [feit.system.core :as system]
   [feit.core.util :refer [->context map-kv top-key]]
   [taoensso.timbre :as timbre]))

(defn- has-handler? [handler-key component]
  ((set (sp/select [:component/handlers MAP-VALS :handler/key] component))
   handler-key))

(defn- filter-handler-components [handler-key components]
  (mapv :component/key
        (filter #(has-handler? handler-key %) components)))

(defn- components->nested-routes [handler-keys components]
  (for [handler-key handler-keys]
    {handler-key (filter-handler-components handler-key components)}))

(defn- routes [entity]
  (let [components (sp/select [:entity/components MAP-VALS] entity)
        handler-keys (sp/select [ALL :component/handlers MAP-KEYS] components)]
    (->> components
         (components->nested-routes handler-keys)
         (apply merge))))

(defn add-routes [entity] []
  (assoc entity :entity/routes (routes entity)))

(defn- entity-component-state [{:entity/keys [components]}]
  (sp/transform [MAP-VALS] :component/state components))

(defn process-refs-entity [{:context/keys [scene-key entity-key] :as opts}]
  (update opts :entity/components (partial preprocess-components scene-key entity-key)))

(defn preprocess-entity [context entity-key entity-opts]
  (-> entity-opts
      (->> (meta-merge (:entity/ref entity-opts)))
      (dissoc :entity/ref)
      (->> (merge context))
      (assoc :entity/key entity-key)
      (process-refs-entity)))

(defn preprocess-entities [scene-key entities]
  (map-kv #(preprocess-entity (->context scene-key %1) %1 %2) entities))

(defn postprocess-entity [entity]
  (-> entity
      add-routes
      (assoc :entity/state (entity-component-state entity))
      (->> ((:entity/init entity) (:entity/key entity)))))

(defmethod system/init-key :feit/entity [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (assoc :entity/key (top-key k)
             :entity/init (system/get-init-key k)
             :entity/halt! (system/get-entity-halt-key k))))

(defn halt! [{:entity/keys [components dynamic key] :as entity}]
  (doseq [[_ component] components]
    ((:component/halt! component) (:component/state component)))
  ((:entity/halt! entity) (:entity/key entity) entity)
  (when dynamic
    (doseq [parent (parents key)]
      (underive key parent))))

(defn suspend! [entity]
  (doseq [[component-key component] (:entity/components entity)]
    (ig/suspend-key! component-key component))
  (ig/suspend-key! (:entity/key entity) entity))
