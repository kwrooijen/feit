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
    [(ig/ref :component.essen.dev/wireframe)]}

   [:essen/component :component.essen.dev/wireframe]
   {:component/tickers [(ig/ref :ticker.essen.dev/wireframe)]}

   [:essen/ticker :ticker.essen.dev/wireframe] {}})

;; TODO Make this generic so we don't have to use pixi / matter
(defmethod ig/init-key :entity.essen.dev/wireframe [_ _opts] identity)

(defmethod ig/init-key :component.essen.dev/wireframe [_ _opts]
  (fn [_context]
    {:wireframe/active? true}))

(defmethod ig/init-key :ticker.essen.dev/wireframe
  [_k _opts]
  (fn ticker-essen-dev--wireframe [{:context/keys [scene-key]} _state]
    (pixi.debug/draw-wireframe (matter/points) scene-key)))

(defn underive-all-from
  "Underive all child keys starting from `k`"
  [k]
  (doseq [descendant (descendants k)
          ancestor (ancestors descendant)]
    (underive descendant ancestor)))

(defonce halted-scenes (atom #{}))

(defn suspend! []
  (reset! halted-scenes (scenes))
  (ig/suspend! @state/system [:essen/scene])
  (doseq [scene-key (scenes)]
    (scene/halt! scene-key)))

(defn resume [config]
  (reset! state/config (system/prep config))
  (-> @state/config
      (system/init  [:essen/scene])
      (->> (reset! state/system)))
  (doseq [scene-key @halted-scenes]
    (scene/start! scene-key)))
