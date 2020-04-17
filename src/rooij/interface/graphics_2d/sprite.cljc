(ns rooij.interface.graphics-2d.sprite
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.config]
   [rooij.state :as state]
   [rooij.interface.graphics-2d.core :refer [make-sprite]]))

(defprotocol RooijGraphics2DSprite
  (play [this spritesheet animation])
  (play-loop [this spritesheet animation opts])
  (stop-loop [this animation])
  (flip [this x y]))

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

(defmethod ig/prep-key :graphics-2d.component/sprite [_ opts]
  (meta-merge
   {:component/handlers [{:handler/ref (ig/ref :general-2d.handler.position/set)}
                         {:handler/ref (ig/ref :graphics-2d.handler.sprite/play)}
                         {:handler/ref (ig/ref :graphics-2d.handler.sprite/play-loop)}
                         {:handler/ref (ig/ref :graphics-2d.handler.sprite/stop-loop)}
                         {:handler/ref (ig/ref :graphics-2d.handler.sprite/flip)}]}
   opts))

(defmethod ig/init-key :graphics-2d.component/sprite [_ opts]
  (make-sprite state/graphics-2d opts))

(it/derive-hierarchy
 {:graphics-2d.component/sprite    [:rooij/component :rooij/position]})

(rooij.config/merge-interface!
 {[:rooij/handler :graphics-2d.handler.sprite/play] {}
  [:rooij/handler :graphics-2d.handler.sprite/play-loop] {}
  [:rooij/handler :graphics-2d.handler.sprite/stop-loop] {}
  [:rooij/handler :graphics-2d.handler.sprite/flip] {}})
