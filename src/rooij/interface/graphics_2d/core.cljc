(ns rooij.interface.graphics-2d.core
  (:require
   [rooij.config]
   [rooij.interface.graphics-2d.entity]
   [rooij.interface.graphics-2d.component]
   [rooij.state :as state]
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.core :as it]))

(def system
  :rooij.interface.graphics-2d/system)

(def scene
  :rooij.interface.graphics-2d/scene)

(defn graphics-2d-enabled? []
  (and (contains? (methods ig/init-key) system)
       (contains? (methods ig/init-key) scene)))

(defn init []
  (when (graphics-2d-enabled?)
    (-> @rooij.config/config
        (ig/prep [system])
        (ig/init [system])
        (it/find-derived-value system)
        (state/set-graphics-2d!))

    (state/set-graphics-2d-scene!
     {:init (ig/init-key scene {})
      :halt! (ig/halt-key! scene {})})))

(defprotocol RooijSprite2D
  (play! [this spritesheet animation]))

(defmethod ig/prep-key :graphics-2d.component/sprite [_ opts]
  (meta-merge
   {:component/handlers [(ig/ref :graphics-2d.handler.sprite/play)]}
   opts))

(defmethod ig/init-key :graphics-2d.handler.sprite/play [_ _opts]
  (fn handler-sprite--play
    [_context {:event/keys [spritesheet animation]} state]
    (play! state spritesheet animation)
    state))

(it/derive-hierarchy
 {:graphics-2d.entity/asset-loader [:rooij/entity]
  :graphics-2d.entity/spritesheet-loader [:rooij/entity]

  :graphics-2d.component/sprite [:rooij/component]
  :graphics-2d.component/rectangle [:rooij/component]})

(rooij.config/merge-extension!
 {[:rooij/handler :graphics-2d.handler.sprite/play] {}})
