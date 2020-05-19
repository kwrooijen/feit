(ns feit.dev
  (:require
   [integrant.core :as ig]
   [feit.api :refer [scenes]]
   [feit.config]
   [feit.dsl :as r]
   [feit.interface.graphics-2d.core :refer [draw-wireframe]]
   [feit.interface.physics-2d.core :refer [get-wireframe-vectors]]
   [feit.core.state :as state]
   [feit.system.core :as system]
   [feit.system.scene :as scene]))

(def config
  {[:feit/entity :entity.feit.dev/wireframe]
   {:entity/components
    {:component.feit.dev/wireframe
     {:component/ref (ig/ref :component.feit.dev/wireframe)}}}

   [:feit/component :component.feit.dev/wireframe]
   {:component/tickers
    {:ticker.feit.dev/wireframe
     {:ticker/ref (ig/ref :ticker.feit.dev/wireframe)}}}

   [:feit/ticker :ticker.feit.dev/wireframe] {}})

(defmethod ig/init-key :entity.feit.dev/wireframe [_ _opts] nil)

(defmethod ig/init-key :component.feit.dev/wireframe [_ _opts]
  {:wireframe/active? true})

(defmethod ig/init-key :ticker.feit.dev/wireframe
  [_k {:context/keys [scene-key]}]
  (fn ticker-feit-dev--wireframe [_context _state]
    (draw-wireframe state/graphics-2d scene-key
                    (get-wireframe-vectors state/physics-2d scene-key))))

(defonce halted-scenes (atom #{}))

(defn suspend! []
  (reset! halted-scenes (scenes))
  (ig/suspend! @state/system [:feit/scene])
  (doseq [scene-key (scenes)]
    (scene/halt! scene-key)))

(defn resume
  ([] (resume {}))
  ([config]
   (r/save! config)
   (system/start)
   (doseq [scene-key @halted-scenes]
     (scene/start! scene-key))))
