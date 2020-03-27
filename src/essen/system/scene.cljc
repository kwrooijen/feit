(ns essen.system.scene
  (:require
   [com.rpl.specter :as specter :refer [MAP-VALS ALL] :refer-macros [transform]]
   [essen.state :as state]
   [essen.util :refer [vec->map spy top-key]]
   [integrant-tools.core :as it]
   [essen.system.entity :as entity]
   [essen.system :as system]
   [essen.system.component :as component]
   [integrant.core :as ig]
   [essen.render]))

(defn- start-entity [scene-key {entity-key :entity/key :as entity}]
  (try (update entity :entity/init (fn [entity-init]
                                     (entity-init {:context/scene scene-key
                                                   :context/entity entity-key})))
       (catch #?(:clj Throwable :cljs :default) t
         (println "[ERROR] Failed to init entity.\n"
                  "Scene:" scene-key "\n"
                  "Entity:" entity-key "\n"
                  "Reason:" (ex-data t)))))

(defmethod system/init-key :essen/scene [k opts]
  ;; TODO save scenes to state and call this. This is currently not being used
  (-> opts
      (assoc :scene/key (top-key k)
             :scene/init (system/get-init-key k opts)
             :scene/halt! (system/get-halt-key k opts))
      (update :scene/entities vec->map :entity/key)))

(defn start-components [scene-key {entity-key :entity/key :as entity}]
  (let [context {:context/scene scene-key
                 :context/entity entity-key}]
    (transform [:entity/components MAP-VALS]
               (comp component/start
                     (partial merge context))
               entity)))

(defn start-entities [opts scene-key]
  (transform [:scene/entities MAP-VALS]
             (comp (partial start-entity scene-key)
                   (partial start-components scene-key))
             opts))

(defn init [scene-key]
  (-> @state/system
      (it/find-derived-value scene-key)
      (start-entities scene-key)
      (assoc :scene/key scene-key)
      (state/save-scene!)))

(defn start!
  ([scene-key] (start! scene-key {}))
  ([scene-key opts]
   (state/reset-events! scene-key)
   (init scene-key)
   (essen.render/init scene-key)))

(defn resume! [scene-key]
  (state/reset-events! scene-key)
  (init scene-key)
  (essen.render/resume scene-key))

(defn halt! [scene-key]
  (essen.render/halt! scene-key)
  (state/reset-events! scene-key)
  (let [{:scene/keys [entities] :as scene} @(state/get-scene scene-key)]
    (doseq [[_ entity] entities]
      (entity/halt! entity)))
  (state/reset-state! scene-key))
