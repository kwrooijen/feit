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
  (draw-wireframe [this scene-key vectors])
  (make-sprite [this opts])
  (make-rectangle [this opts]))

(defprotocol RooijGraphics2DSprite
  (play! [this spritesheet animation]))

(defprotocol RooijGraphics2DRectangle)

(deftype DefaultGraphics2D []
  RooijGraphics2D
  (scene-init [this scene-key] nil)
  (scene-halt! [this scene-key] nil)
  (step [this scene-key] nil)
  (draw-wireframe [this scene-key vectors] nil))

(def system
  :rooij.interface.graphics-2d/system)

(defn init []
  (-> @rooij.config/config
      (meta-merge {system {}})
      (ig/prep [system])
      (ig/init [system])
      (it/find-derived-value system)
      (or (DefaultGraphics2D.))
      (state/set-graphics-2d!)))

(defmethod ig/init-key :graphics-2d.handler.sprite/play [_ _opts]
  (fn handler-sprite--play
    [_context {:event/keys [spritesheet animation]} state]
    (play! state spritesheet animation)
    state))

(defmethod ig/prep-key :graphics-2d.component/sprite [_ opts]
  (meta-merge
   {:component/handlers [{:handler/ref (ig/ref :general-2d.handler.position/set)}
                         {:handler/ref (ig/ref :graphics-2d.handler.sprite/play)}]}
   opts))

(defmethod ig/init-key :graphics-2d.component/sprite [_ opts]
  (make-sprite state/graphics-2d opts))

(defmethod ig/prep-key :graphics-2d.component/rectangle [_ opts]
  (meta-merge
   {:component/handlers [{:handler/ref (ig/ref :general-2d.handler.position/set)}]}
   opts))

(defmethod ig/init-key :graphics-2d.component/rectangle [_ opts]
  (make-rectangle state/graphics-2d opts))

(it/derive-hierarchy
 {:graphics-2d.entity/asset-loader [:rooij/entity]
  :graphics-2d.entity/spritesheet-loader [:rooij/entity]

  :graphics-2d.component/sprite    [:rooij/component :rooij/position]
  :graphics-2d.component/rectangle [:rooij/component :rooij/position]})

(rooij.config/merge-interface!
 {[:rooij/handler :graphics-2d.handler.sprite/play] {}})
