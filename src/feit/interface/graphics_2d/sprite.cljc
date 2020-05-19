(ns feit.interface.graphics-2d.sprite
  (:require
   [feit.dsl :as r]
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [feit.config]
   [feit.state :as state]
   [feit.interface.graphics-2d.core :refer [make-sprite]]))

(defprotocol FeitGraphics2DSprite
  (play [this spritesheet animation])
  (play-loop [this spritesheet animation opts])
  (stop-loop [this animation])
  (flip [this x y])
  (halt! [this]))

(defmethod ig/init-key :graphics-2d.handler.sprite/play [_ _opts]
  (fn handler-sprite--play
    [_context {:event/keys [spritesheet animation]} state]
    (play state spritesheet animation)))

(defmethod ig/init-key :graphics-2d.handler.sprite/play-loop [_ _opts]
  (fn handler-sprite--play-loop
    [_context {:event/keys [spritesheet animation] :as opts} state]
    (play-loop state spritesheet animation opts)))

(defmethod ig/init-key :graphics-2d.handler.sprite/stop-loop [_ _opts]
  (fn handler-sprite--stop-loop
    [_context {:event/keys [animation]} state]
    (stop-loop state animation)))

(defmethod ig/init-key :graphics-2d.handler.sprite/flip [_ _opts]
  (fn handler-sprite--flip
    [_context {:event/keys [x y]} state]
    (flip state x y)))

(defmethod ig/init-key :graphics-2d.component/sprite [_ opts]
  (make-sprite state/graphics-2d opts))

(defmethod ig/halt-key! :graphics-2d.component/sprite [_ _opts]
  (fn [state]
    (halt! state)))

(it/derive-hierarchy
 {:graphics-2d.component/sprite [:feit/component :feit/position]})

(-> (r/component :graphics-2d.component/sprite)
    (r/handler+ref :graphics-2d.handler.sprite/play)
    (r/handler+ref :graphics-2d.handler.sprite/play-loop)
    (r/handler+ref :graphics-2d.handler.sprite/stop-loop)
    (r/handler+ref :graphics-2d.handler.sprite/flip)
    (r/ref-handler :general-2d.handler.position/set)
    (r/save-interface!))
