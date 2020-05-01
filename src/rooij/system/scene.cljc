(ns rooij.system.scene
  (:require
   [rooij.config]
   [rooij.interface.graphics-2d.core :as interface.graphics-2d]
   [rooij.interface.physics-2d.core :as interface.physics-2d]
   [taoensso.timbre :as timbre]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [rooij.state :as state]
   [rooij.util :refer [top-key resolve-all]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [descendant?]]
   [rooij.system.keyboard :as keyboard]
   [rooij.system.entity :as entity]
   [rooij.system.core :as system]
   [rooij.system.component :as component]))

(defn change-keyboard-identifier
  "Regular systems identify by their integrant key. It's different for keyboard
  events because we look them up by their keycode. Use [:key/down keycode] or
  [:key/up keycode] as the identifier instead."
  [keyboard]
  (into {}
        (for [[_ v] keyboard]
          (cond
            (:keyboard-down/key v)       [[:key/down       (:keyboard-down/key v)] v]
            (:keyboard-up/key v)         [[:key/up         (:keyboard-up/key v)] v]
            (:keyboard-while-down/key v) [[:key/while-down (:keyboard-while-down/key v)] v]))))

(defmethod system/init-key :rooij/scene [k opts]
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

(defn start-entities [opts scene-key]
  (let [context {:context/scene-key scene-key}]
    (sp/transform [:scene/entities MAP-VALS]
                  (comp entity/init
                        (partial merge context)
                        (partial start-components scene-key))
                  opts)))

(defn prepare-init-config [scene-key extra-opts opts]
  (with-meta
    {scene-key (merge extra-opts opts)}
    {:scene/last [scene-key]}))

(defn apply-init [scene-opts scene-key extra-opts]
  (-> (prepare-init-config scene-key extra-opts scene-opts)
      (->> ((:scene/init scene-opts) scene-key))
      (get scene-key)
      (resolve-all)))

(defn start-keyboard [system]
  (update system :scene/keyboard #(sp/transform [MAP-VALS] keyboard/init %)))

(defn init [scene-key opts]
  (timbre/debug ::init opts)
  (-> @state/system
      (it/find-derived-value scene-key)
      (apply-init scene-key opts)
      (update :scene/entities system/process-refs :entity)
      (update :scene/keyboard system/process-refs :keyboard)
      (update :scene/keyboard change-keyboard-identifier)
      (start-entities scene-key)
      (start-keyboard)
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
  (if-let [scene-key (:rooij/initial-scene @rooij.config/config)]
    (start! scene-key)
    (throw (ex-info "No initial scene" {:reason ::no-initial-scene}))))
