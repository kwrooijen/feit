(ns essen.dev
  (:require
   [essen.core :refer [scenes]]
   [essen.system.scene :as scene]
   [essen.system.entity :as entity]
   [essen.render]
   [essen.state :as state :refer [game]]))

(defn suspend! []
  (doseq [scene-key (scenes)]
    (essen.render/run scene-key :essen/stage-suspend))
  (doseq [scene-key (scenes)
          [_entity-key entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/suspend! entity)))

(defn resume [config]
  (swap! game assoc :essen/config (essen.core/prep config))
  (doseq [scene-key (scenes)]
    ;; TODO resume instead of start ?
    (essen.render/run scene-key :essen/stage-resume)
    (scene/start! scene-key {} {:dev true})))
