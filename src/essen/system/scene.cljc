(ns essen.system.scene
  (:require
   [com.rpl.specter :as specter :refer [MAP-VALS ALL] :refer-macros [transform]]
   [essen.state :as state]
   [essen.util :refer [vec->map spy top-key]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [make-child]]
   [essen.system.entity :as entity]
   [essen.system :as system]
   [essen.system.component :as component]
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
  (-> opts
      (assoc :scene/key (top-key k)
             :scene/init (system/get-init-key k opts)
             :scene/halt! (system/get-halt-key k opts))))

(defn start-components [scene-key {entity-key :entity/key :as entity}]
  (let [context {:context/scene scene-key
                 :context/entity entity-key}]
    (transform [:entity/components MAP-VALS]
               (comp component/start
                     #(update % :component/opts merge context))
               entity)))

(defn make-dynamic-entity [entity]
  (if (:entity/dynamic entity)
    (update entity :entity/key make-child)
    entity))

(defn entities->map [entities]
  (->> (flatten entities)
       (map make-dynamic-entity)
       (map (juxt :entity/key identity))
       (into {})))

(defn start-entities [opts scene-key]
  (->> opts
       (transform [:scene/entities] entities->map)
       (transform [:scene/entities MAP-VALS]
                  (comp (partial start-entity scene-key)
                        (partial start-components scene-key)))))

(defn apply-init [scene-opts opts]
  ((:scene/init scene-opts) scene-opts opts))

(defn init [scene-key opts]
  (-> @state/system
      (it/find-derived-value scene-key)
      (apply-init opts)
      (start-entities scene-key)
      (assoc :scene/key scene-key)
      (state/save-scene!)))

(defn start!
  ([scene-key] (start! scene-key {}))
  ([scene-key opts]
   (state/reset-events! scene-key)
   (init scene-key opts)
   (essen.render/init scene-key)))

(defn resume! [scene-key]
  (state/reset-events! scene-key)
  (init scene-key {})
  (essen.render/resume scene-key))

(defn halt! [scene-key]
  (essen.render/halt! scene-key)
  (state/reset-events! scene-key)
  (doseq [[_ entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/halt! entity))
  (state/reset-state! scene-key))
