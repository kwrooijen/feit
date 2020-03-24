(ns essen.dev
  (:require
   [essen.core :refer [scenes]]
   [essen.module.matterjs :as matter]
   [essen.module.pixi.debug :as pixi.debug]
   [essen.render]
   [essen.state :as state]
   [essen.system :as system]
   [essen.system.entity :as entity]
   [essen.system.scene :as scene]
   [integrant.core :as ig]))

(def config
  {[:essen/entity :entity.essen.dev/wireframe]
   {:entity/components
    [(ig/ref :component.essen.dev/wireframe)]

    [:essen/component :component.essen.dev/wireframe]
    {:component/tickers [(ig/ref :ticker.essen.dev/wireframe)]}

    [:essen/ticker :ticker.essen.dev/wireframe] {}}})

;; TODO Make this generic so we don't have to use pixi / matter
(defmethod ig/init-key :entity.essen.dev/wireframe [_ _opts] identity)

(defmethod ig/init-key :component.essen.dev/wireframe [_ _opts]
  (fn [_context]
    {:wireframe/active? true}))

(defmethod ig/init-key :ticker.essen.dev/wireframe
  [_k _opts]
  (fn ticker-essen-dev--wireframe [{:context/keys [scene] :as _subs} _component _ticker _state]
    (pixi.debug/draw-wireframe (matter/points) scene)))

(defn underive-all-from
  "Underive all child keys starting from `k`"
  [k]
  (doseq [descendant (descendants k)
          ancestor (ancestors descendant)]
    (underive descendant ancestor)))

(defn suspend! []
  (doseq [scene-key (scenes)]
    (essen.render/suspend! scene-key))
  (doseq [scene-key (scenes)
          [_entity-key entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/suspend! entity)))

(defn resume [config]
  (reset! state/config (system/prep config))
  (entity/prep)
  (doseq [scene-key (scenes)]
    (scene/resume! scene-key)))
