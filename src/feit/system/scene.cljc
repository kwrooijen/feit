(ns feit.system.scene
  (:require
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [descendant?]]
   feit.config
   [feit.interface.graphics-2d.core :as interface.graphics-2d]
   [feit.interface.physics-2d.core :as interface.physics-2d]
   [feit.core.state :as state]
   [feit.system.core :as system]
   [feit.system.entity :as entity
    :refer [postprocess-entity preprocess-entities]]
   [feit.core.util :refer [resolve-all top-key]]
   [taoensso.timbre :as timbre]))

(defmethod system/init-key :feit/scene [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :scene/key (top-key k)
         :scene/init (system/get-init-key k)
         :scene/halt! (system/get-halt-key k opts)))

(defn- prepare-init-config [scene-key extra-opts opts]
  (with-meta
    {scene-key (merge extra-opts opts)}
    {:scene/last [scene-key]}))

(defn- apply-init [scene-opts scene-key extra-opts]
  (-> (prepare-init-config scene-key extra-opts scene-opts)
      (->> ((:scene/init scene-opts) scene-key))
      (get scene-key)
      (resolve-all)))

(defn process-refs [{scene-key :scene/key :as opts}]
  (->> opts
       (sp/transform [:scene/entities] (partial preprocess-entities scene-key))
       (sp/transform [:scene/entities MAP-VALS] postprocess-entity)))

(defn init [scene-key opts]
  (timbre/debug ::init opts)
  (-> @state/system
      (it/find-derived-value scene-key)
      (apply-init scene-key opts)
      (process-refs)
      (assoc :scene/key scene-key)
      (state/save-scene!)))

(defn validate-scene [scene-key]
  (when-not (descendant? scene-key :feit/scene)
    (throw (ex-info (str "Scene not found: " scene-key) {:scene/key scene-key}))))

(defn start!
  ([scene-key] (start! scene-key {}))
  ([scene-key opts]
   (timbre/debug ::start! opts)
   (validate-scene scene-key)
   (interface.graphics-2d/scene-init state/graphics-2d scene-key)
   (interface.physics-2d/scene-init state/physics-2d scene-key)
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
  (interface.physics-2d/scene-halt! state/physics-2d scene-key))

(defn start-initial-scene []
  (if-let [scene-key (:feit/initial-scene @feit.config/config)]
    (start! scene-key)
    (throw (ex-info "No initial scene" {:reason ::no-initial-scene}))))
