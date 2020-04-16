(ns rooij.interface.graphics-2d.core
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.config]
   [rooij.interface.graphics-2d.component]
   [rooij.interface.graphics-2d.entity]
   [rooij.interface.graphics-2d.interface :refer [DefaultGraphics2D play! make-rectangle make-sprite]]
   [rooij.interface.graphics-2d.interface.loader]
   [rooij.state :as state]))

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
 {[:rooij/handler :graphics-2d.handler.sprite/play] {}
  [:rooij/handler :graphics-2d.handler.loader/load-complete] {}
  [:rooij/handler :graphics-2d.handler.loader/load-texture] {}
  [:rooij/handler :graphics-2d.handler.loader/load-spritesheet] {}})
