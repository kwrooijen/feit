(ns rooij.interface.graphics-2d.core
  (:require
   [rooij.interface.graphics-2d.entity]
   [rooij.interface.graphics-2d.component]
   [rooij.state :as state]
   [integrant.core :as ig]
   [integrant-tools.core :as it]))

(def system
  :rooij.interface.graphics-2d/system)

(def scene
  :rooij.interface.graphics-2d/scene)

(defn graphics-2d-enabled? []
  (and (contains? ig/init-key system)
       (contains? ig/init-key scene)))

(defn init [config]
  (when  (graphics-2d-enabled?)
      (-> config
          (ig/prep [system])
          (ig/init [system])
          (it/find-derived-value system)
          (state/set-graphics-2d!))

      (state/set-graphics-2d-scene!
       {:init (ig/init-key scene {})
        :halt! (ig/halt-key! scene {})})))

(it/derive-hierarchy
 {:graphics-2d.entity/asset-loader [:rooij/entity]
  :graphics-2d.entity/spritesheet-loader [:rooij/entity]

  :graphics-2d.component/sprite [:rooij/component]})

(def config
  ;; TODO remove pixi
  {[:rooij/handler :handler.pixi.sprite/play] {}})
