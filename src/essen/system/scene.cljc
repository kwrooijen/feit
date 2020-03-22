(ns essen.system.scene
  (:require
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.state :as state]
   [essen.util :refer [vec->map spy top-key]]
   [integrant-tools.core :as it]
   [essen.system.entity :as entity]
   [essen.system :as system]
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

(defn maybe-init-key [derived-k opts]
  (if-let [f (get-method ig/init-key (ig/normalize-key derived-k))]
    (f derived-k opts)
    opts))

(defmethod system/init-key :essen/scene [k opts]
  (-> (maybe-init-key k opts)
      (assoc :scene/key (top-key k))))

(defn post-init [system]
  (->> system
       (transform [:scene/entities MAP-VALS :entity/components MAP-VALS]
                  :component/state)
       (system/post-init-key!))
  system)

(defn start!
  ([scene-key] (start! scene-key {} {}))
  ([scene-key opts] (start! scene-key opts {}))
  ([scene-key opts extra]
   (when-not (:dev extra)
     (essen.render/init scene-key))
   (state/reset-events! scene-key)
   (-> @state/config
       (add-defaults scene-key opts nil)
       (system/init [scene-key])
       (it/find-derived-value scene-key)
       (update :scene/entities entities-fn @state/config scene-key opts)
       (post-init)
       (state/save-scene!))))

(defn halt! [scene-key]
  (essen.render/halt! scene-key)
  (doseq [[_ entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/halt! entity))
  (state/reset-events! scene-key)
  (state/reset-state! scene-key))
