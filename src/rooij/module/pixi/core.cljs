(ns rooij.module.pixi.core
  (:require
   [rooij.module.pixi.entity]
   [rooij.module.pixi.state :as state]
   [integrant.core :as ig]
   [rooij.module.pixi.interface :refer [->PixiGraphics2D]]))

(defn setup-event-listener-resize []
  (let [handler #(.resize state/renderer
                          (.-innerWidth js/window)
                          (.-innerHeight js/window))]
    (js/setTimeout handler 100)
    (.addEventListener js/window "resize" handler)))

(defn setup-renderer
  [{:graphics-2d.window/keys [view width height auto-dencity]
    :or {view        "game"
         width       (.-innerWidth js/window)
         height      (.-innerHeight js/window)
         auto-dencity true}}]
  (state/set-renderer!
   {:view        (js/document.getElementById view)
    :width       width
    :height      height
    :transparent true
    :autoDencity auto-dencity}))

(defmethod ig/init-key :rooij.interface.graphics-2d/system [_ init-opts]
  (setup-event-listener-resize)
  (setup-renderer init-opts)
  (->PixiGraphics2D init-opts))
