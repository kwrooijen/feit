(ns rooij.system.scene
  (:require
   [rooij.config]
   [rooij.interface.graphics-2d.core :as interface.graphics-2d]
   [taoensso.timbre :as timbre]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [rooij.state :as state]
   [rooij.util :refer [top-key]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [descendant?]]
   [rooij.system.entity :as entity]
   [rooij.system.core :as system]
   [rooij.system.component :as component]))

(defmethod system/init-key :rooij/scene [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (update :scene/entities system/process-refs :entity)
      (assoc :scene/key (top-key k)
             :scene/init (system/get-init-key k)
             :scene/halt! (system/get-halt-key k opts))))

(defn start-components [scene-key {entity-key :entity/key :as entity}]
  (let [context {:context/scene-key scene-key
                 :context/entity-key entity-key}]
    (sp/transform [:entity/components MAP-VALS]
                  (comp component/init
                        #(component/prep % context))
                  entity)))

(defn start-entities [opts scene-key]
  (let [context {:context/scene-key scene-key}]
    (sp/transform [:scene/entities MAP-VALS]
                  (comp entity/init
                        #(update % :entity/opts merge context)
                        (partial start-components scene-key))
                  opts)))

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
  (when-not (descendant? scene-key :rooij/scene)
    (throw (ex-info (str "Scene not found: " scene-key) {:scene/key scene-key}))))

(defn start!
  ([scene-key] (start! scene-key {}))
  ([scene-key opts]
   (timbre/debug ::start! opts)
   (validate-scene scene-key)
   (interface.graphics-2d/scene-init state/graphics-2d scene-key)
   ((:init state/physics-2d-scene) scene-key)
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
  (interface.graphics-2d/scene-halt! state/graphics-2d scene-key)
  ((:halt! state/physics-2d-scene) scene-key))

(defn start-initial-scene []
  (if-let [scene-key (:rooij/initial-scene @rooij.config/config)]
    (start! scene-key)
    (throw (ex-info "No initial scene" {:reason ::no-initial-scene}))))
