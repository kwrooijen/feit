(ns essen.dev
  (:require
   [essen.core :refer [scenes]]
   [essen.system.scene :as scene]
   [essen.system.entity :as entity]
   [essen.system :as system]
   [essen.render]
   [essen.state :as state]))

(defn suspend! []
  (doseq [scene-key (scenes)]
    (essen.render/suspend! scene-key))
  (doseq [scene-key (scenes)
          [_entity-key entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/suspend! entity)))

(defn resume [config]
  (reset! state/config (system/prep config))
  (doseq [scene-key (scenes)]
    (essen.render/resume scene-key)
    (scene/start! scene-key {} {:dev true})))
