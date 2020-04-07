(ns essen.system.scene
  (:require
   [taoensso.timbre :as timbre]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [essen.state :as state]
   [essen.util :refer [top-key]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [make-child descendant?]]
   [essen.system.entity :as entity]
   [essen.system.core :as system]
   [essen.system.component :as component]))

(defmethod system/init-key :essen/scene [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :scene/key (top-key k)
         :scene/init (system/get-init-key k)
         :scene/halt! (system/get-halt-key k opts)))

(defn start-components [scene-key {entity-key :entity/key :as entity}]
  (let [context {:context/scene-key scene-key
                 :context/entity-key entity-key}]
    (sp/transform [:entity/components MAP-VALS]
                  (comp component/init
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
  (let [context {:context/scene-key scene-key}]
    (->> opts
        (sp/transform [:scene/entities] entities->map)
        (sp/transform [:scene/entities MAP-VALS]
                      (comp entity/init
                            #(update % :entity/opts merge context)
                            (partial start-components scene-key))))))

(defn apply-init [scene-opts scene-key opts]
  ((:scene/init scene-opts) scene-key (merge scene-opts opts)))

(defn init [scene-key opts]
  (timbre/debug ::init opts)
  (-> @state/system
      (it/find-derived-value scene-key)
      (apply-init scene-key opts)
      (start-entities scene-key)
      (assoc :scene/key scene-key)
      (state/save-scene!)))

(defn validate-scene [scene-key]
  (when-not (descendant? scene-key :essen/scene)
    (throw (ex-info (str "Scene not found: " scene-key) {:scene/key scene-key}))))

(defn start!
  ([scene-key] (start! scene-key {}))
  ([scene-key opts]
   (timbre/debug ::start! opts)
   (validate-scene scene-key)
   ((:init state/graphics-2d-scene) scene-key)
   (state/reset-events! scene-key)
   (init scene-key opts)))

(defn resume! [scene-key]
  (state/reset-events! scene-key)
  (init scene-key {}))

(defn halt! [scene-key]
  (state/reset-events! scene-key)
  (doseq [[_ entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/halt! entity))
  (state/remove-scene! scene-key)
  ((:halt! state/graphics-2d-scene) scene-key))
