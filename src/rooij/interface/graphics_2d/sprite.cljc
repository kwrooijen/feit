(ns rooij.interface.graphics-2d.sprite
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.config]
   [rooij.state :as state]
   [rooij.interface.graphics-2d.core :refer [make-sprite]]))

(defprotocol RooijGraphics2DSprite
  (play! [this spritesheet animation]))

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

(it/derive-hierarchy
 {:graphics-2d.component/sprite    [:rooij/component :rooij/position]})

(rooij.config/merge-interface!
 {[:rooij/handler :graphics-2d.handler.sprite/play] {}})
