(ns essen.system.scene
  (:require
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [essen.state :as state]
   [essen.util :refer [top-key]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [make-child]]
   [essen.system.entity :as entity]
   [essen.system.core :as system]
   [essen.system.component :as component]
   [essen.render]))

(defmethod system/init-key :essen/scene [k opts]
  (assoc opts
         :scene/key (top-key k)
         :scene/init (system/get-init-key k opts)
         :scene/halt! (system/get-halt-key k opts)))

(defn start-components [scene-key {entity-key :entity/key :as entity}]
  (let [context {:context/scene-key scene-key
                 :context/entity-key entity-key}]
    (sp/transform [:entity/components MAP-VALS]
                  (comp component/start
                        #(component/prep % context))
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
       (sp/transform [:scene/entities] entities->map)
       (sp/transform [:scene/entities MAP-VALS]
                  (comp (partial entity/init scene-key)
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
   ((:init state/graphics-2d-scene) scene-key)
   (state/reset-events! scene-key)
   (init scene-key opts)))

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
