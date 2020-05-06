(ns rooij.module.pixi.core
  (:require [integrant.core :as ig]
            [rooij.module.pixi.interface :refer [->PixiGraphics2D]]
            [rooij.module.pixi.interface.loader]
            [rooij.module.pixi.state :as state]
            [rooij.module.pixi.screen :as pixi.screen]))

(defn- setup-renderer
  [{:graphics-2d.window/keys [view width height auto-dencity scale-x scale-y]
    :or {view        "game"
         width       (.-innerWidth js/window)
         height      (.-innerHeight js/window)
         auto-dencity true}}]
  (state/set-renderer!
   {:view        (js/document.getElementById view)
    ;; TODO add scale boolean instead of scale-x scale-y
    :width       scale-x
    :height      scale-y
    ;; :transparent true
    :autoDencity auto-dencity}))

(defmethod ig/init-key :rooij.interface.graphics-2d/system [_ init-opts]
  (pixi.screen/setup-event-listener-resize init-opts)
  (setup-renderer init-opts)
  (->PixiGraphics2D init-opts))
