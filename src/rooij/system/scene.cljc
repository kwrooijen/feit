(ns rooij.system.scene
  (:require [com.rpl.specter :as sp :refer [MAP-VALS]]
            [integrant-tools.core :as it]
            [integrant-tools.keyword :refer [descendant?]]
            rooij.config
            [rooij.interface.graphics-2d.core :as interface.graphics-2d]
            [rooij.interface.physics-2d.core :as interface.physics-2d]
            [rooij.state :as state]
            [rooij.system.component :refer [process-refs-component]]
            [rooij.system.core :as system]
            [rooij.system.entity :as entity
             :refer [postprocess-entity preprocess-entities process-refs-entity]]
            [rooij.system.keyboard :as keyboard]
            [rooij.util :refer [resolve-all top-key]]
            [taoensso.timbre :as timbre]))

(defmethod system/init-key :rooij/scene [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :scene/key (top-key k)
         :scene/init (system/get-init-key k)
         :scene/halt! (system/get-halt-key k opts)))

(defn- change-keyboard-identifier
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

(defn- prepare-init-config [scene-key extra-opts opts]
  (with-meta
    {scene-key (merge extra-opts opts)}
    {:scene/last [scene-key]}))

(defn- apply-init [scene-opts scene-key extra-opts]
  (-> (prepare-init-config scene-key extra-opts scene-opts)
      (->> ((:scene/init scene-opts) scene-key))
      (get scene-key)
      (resolve-all)))

(defn- start-keyboard [system]
  (update system :scene/keyboard #(sp/transform [MAP-VALS] keyboard/init %)))

(defn process-refs [{scene-key :scene/key :as opts}]
  (->> opts
       (sp/transform [:scene/entities] (partial preprocess-entities scene-key))
       (sp/transform [:scene/entities MAP-VALS] process-refs-entity)
       (sp/transform [:scene/entities MAP-VALS :entity/components MAP-VALS] process-refs-component)
       (sp/transform [:scene/entities MAP-VALS] postprocess-entity)))

(defn init [scene-key opts]
  (timbre/debug ::init opts)
  (-> @state/system
      (it/find-derived-value scene-key)
      (apply-init scene-key opts)
      (process-refs)
      (update :scene/keyboard change-keyboard-identifier)
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
