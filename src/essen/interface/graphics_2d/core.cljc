(ns essen.interface.graphics-2d.core
  (:require
   [essen.interface.graphics-2d.entity]
   [essen.interface.graphics-2d.component]
   [essen.state :as state]
   [integrant.core :as ig]
   [integrant-tools.core :as it]))

(def system
  :essen.interface.graphics-2d/system)

(def scene
  :essen.interface.graphics-2d/scene)

(defn init [config]
  (-> config
      (ig/prep [system])
      (ig/init [system])
      (it/find-derived-value system)
      (state/set-graphics-2d!))

  (state/set-graphics-2d-scene!
   {:init (-> scene descendants first (ig/init-key {}))
    :halt! (-> scene descendants first (ig/halt-key! {}))}))

(it/derive-hierarchy
 {:graphics-2d.entity/asset-loader [:essen/entity]
  :graphics-2d.entity/spritesheet-loader [:essen/entity]

  :graphics-2d.component/sprite [:essen/component]})
