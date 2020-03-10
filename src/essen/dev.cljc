(ns essen.dev
  (:require
   [essen.core :refer [scenes]]
   [essen.system.scene :as scene]
   [essen.render]
   [essen.state :as state :refer [game]]
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(defn resume-scene [scene-key]
  ;; FIXME This doesn't work
  ;; Would be nice to keep the state, but replacee the :*/fn functions.
  (-> (state/config)
      (it/prep [:it/prep-meta :ig/prep] [scene-key])
      (ig/resume (state/system scene-key) [scene-key])
      (state/save-system!  scene-key))
  (state/reset-events! scene-key)
  (state/save-state! scene-key)
  (essen.render/run scene-key :essen/stage-resume))

(defn suspend-scene [scene-key]
  (essen.render/run scene-key :essen/stage-suspend))

(defn suspend! []
  (doseq [scene (scenes)]
    (suspend-scene scene)))

(defn resume [config]
  (swap! game assoc :essen/config (ig/prep config))
  (doseq [scene (scenes)]
    ;; TODO scene/opts get lost after `resume`..
    ;; Does that actually matter once we implement a proper resume?
    ;; Right now it should be fixed though
    ;; FIXME `resume-scene` needs to eb fixed, and should be called here
    (scene/start! scene)))
