(ns feit.module.pixi.core
  (:require [integrant.core :as ig]
            [feit.module.pixi.interface :refer [->PixiGraphics2D]]
            [feit.module.pixi.interface.loader]
            [feit.module.pixi.state :as state]
            [feit.module.pixi.screen :as pixi.screen]))

(defn- setup-renderer
  [{:graphics-2d.window/keys [view width height auto-dencity]
    :or {view        "game"
         width       (.-innerWidth js/window)
         height      (.-innerHeight js/window)
         auto-dencity true}}]
  (state/set-renderer!
   {:view        (js/document.getElementById view)
    :width       width
    :height      height
    ;; :transparent true
    :autoDencity auto-dencity}))

(defmethod ig/init-key :feit.interface.graphics-2d/system [_ init-opts]
  (pixi.screen/setup-event-listener-resize init-opts)
  (setup-renderer init-opts)
  (->PixiGraphics2D init-opts))
