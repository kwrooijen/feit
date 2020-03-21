(ns essen.system.scene
  (:require
   [essen.state :as state]
   [essen.util :refer [vec->map spy top-key]]
   [integrant-tools.core :as it]
   [essen.system.entity :as entity]
   [essen.system :as es]
   [integrant.core :as ig]
   [essen.render]))

(defn- add-defaults
  [m scene-key opts entity]
  (assoc m
         [:it/const :context/scene] scene-key
         [:it/const :context/entity] entity
         [:it/const :scene/opts] opts))

(defn- start-entity [config scene-key opts entity]
  (-> config
      (add-defaults scene-key opts entity)
      (entity/start entity)))

(defn- entities-fn [entities config scene-key opts]
  (-> (map (partial start-entity config scene-key opts)
           (flatten entities))
      (vec->map :entity/key)))

(defmethod es/init-key :essen/scene [k opts]
  (-> (ig/init-key k opts)
      (assoc :scene/key (top-key k))))

(defn start!
  ([scene-key] (start! scene-key {} {}))
  ([scene-key opts] (start! scene-key opts {}))
  ([scene-key opts extra]
   (when-not (:dev extra)
     (essen.render/init scene-key))
   (state/reset-events! scene-key)
   (-> @state/config
       (add-defaults scene-key opts nil)
       (es/init [scene-key])
       (it/find-derived-value scene-key)
       (update :scene/entities entities-fn @state/config scene-key opts)
       (state/save-scene!))))

(defn halt! [scene-key]
  (essen.render/halt! scene-key)
  (doseq [[_ entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/halt! entity))
  (state/reset-events! scene-key)
  (state/reset-state! scene-key))
