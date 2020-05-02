(ns rooij.dev
  (:require
   [integrant.core :as ig]
   [rooij.api :refer [scenes]]
   [rooij.config]
   [rooij.dsl :as r]
   [rooij.interface.graphics-2d.core :refer [draw-wireframe]]
   [rooij.interface.physics-2d.core :refer [get-wireframe-vectors]]
   [rooij.state :as state]
   [rooij.system.core :as system]
   [rooij.system.scene :as scene]))

(def config
  {[:rooij/entity :entity.rooij.dev/wireframe]
   {:entity/components
    {:component.rooij.dev/wireframe
     {:component/ref (ig/ref :component.rooij.dev/wireframe)}}}

   [:rooij/component :component.rooij.dev/wireframe]
   {:component/tickers
    {:ticker.rooij.dev/wireframe
     {:ticker/ref (ig/ref :ticker.rooij.dev/wireframe)}}}

   [:rooij/ticker :ticker.rooij.dev/wireframe] {}})

(defmethod ig/init-key :entity.rooij.dev/wireframe [_ _opts] nil)

(defmethod ig/init-key :component.rooij.dev/wireframe [_ _opts]
  {:wireframe/active? true})

(defmethod ig/init-key :ticker.rooij.dev/wireframe
  [_k _opts]
  (fn ticker-rooij-dev--wireframe [{:context/keys [scene-key]} _state]
    (draw-wireframe state/graphics-2d scene-key
                    (get-wireframe-vectors state/physics-2d scene-key))))

(defonce halted-scenes (atom #{}))

(defn suspend! []
  (reset! halted-scenes (scenes))
  (ig/suspend! @state/system [:rooij/scene])
  (doseq [scene-key (scenes)]
    (scene/halt! scene-key)))

(defn resume
  ([] (resume {}))
  ([config]
   (r/save! config)
   (system/start)
   (doseq [scene-key @halted-scenes]
     (scene/start! scene-key))))
