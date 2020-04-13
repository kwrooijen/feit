(ns rooij.module.pixi.core
  (:require
   [rooij.module.pixi.entity]
   [rooij.module.pixi.state :as state]
   [rooij.module.pixi.component.sprite]
   [rooij.module.pixi.component.rectangle]
   [integrant.core :as ig]))

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
   {:view (js/document.getElementById view)
    :width       width
    :height      height
    :transparent true
    ;; For some reason this resolution doubles screen width
    ;; :resolution  resolution
    :autoDencity auto-dencity}))

(defmethod ig/init-key :rooij.interface.graphics-2d/scene  [_ _opts]
  (fn init-pixi-scene [scene-key]
    (state/init-scene! scene-key)))

(defmethod ig/halt-key! :rooij.interface.graphics-2d/scene [_ _opts]
  (fn halt-pixi-scene [scene-key]
    (state/halt-scene! scene-key)))

(defmethod ig/init-key :rooij.interface.graphics-2d/system [_ opts]
  (setup-event-listener-resize)
  (setup-renderer opts)
  (fn [scene-key]
    (.render state/renderer (state/get-scene scene-key))))
