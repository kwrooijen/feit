(ns rooij.interface.graphics-2d.core
  (:require
   [rooij.config]
   [rooij.interface.graphics-2d.entity]
   [rooij.interface.graphics-2d.component]
   [rooij.state :as state]
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.core :as it]))

(defprotocol RooijGraphics2D
  (scene-init [this scene-key])
  (scene-halt! [this scene-key])
  (step [this scene-key])
  (draw-wireframe [this scene-key vectors]))

(deftype DefaultGraphics2D []
  RooijGraphics2D
  (scene-init [this scene-key] nil)
  (scene-halt! [this scene-key] nil)
  (step [this scene-key] nil)
  (draw-wireframe [this scene-key vectors] nil))

(def system
  :rooij.interface.graphics-2d/system)

(def scene
  :rooij.interface.graphics-2d/scene)

(defn graphics-2d-enabled? []
  (and (contains? (methods ig/init-key) system)
       (contains? (methods ig/init-key) scene)))

(defn init []
  (-> @rooij.config/config
      (ig/prep [system])
      (ig/init [system])
      (it/find-derived-value system)
      (or (DefaultGraphics2D.))
      (state/set-graphics-2d!)))

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
