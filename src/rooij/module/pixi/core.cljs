(ns rooij.module.pixi.core
  (:require
   [rooij.interface.graphics-2d.core :refer [RooijGraphics2D]]
   [rooij.module.pixi.debug :as pixi.debug]
   [rooij.module.pixi.entity]
   [rooij.module.pixi.state :as state]
   [rooij.module.pixi.component.sprite :as component.sprite]
   [rooij.module.pixi.component.rectangle :as component.rectangle]
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
   {:view        (js/document.getElementById view)
    :width       width
    :height      height
    :transparent true
    ;; :autoDencity auto-dencity
    }))

(deftype PixiGraphics2D [init-opts]
  RooijGraphics2D
  (scene-init [this scene-key]
    (state/init-scene! scene-key))
  (scene-halt! [this scene-key]
    (state/halt-scene! scene-key) )
  (step [this scene-key]
    (.render state/renderer (state/get-scene scene-key)))
  (draw-wireframe [this scene-key vectors]
    (pixi.debug/draw-wireframe scene-key vectors))
  (make-sprite [this opts] (component.sprite/make-sprite opts))
  (make-rectangle [this opts] (component.rectangle/make-rectangle opts)))

(defmethod ig/init-key :rooij.interface.graphics-2d/system [_ init-opts]
  (setup-event-listener-resize)
  (setup-renderer init-opts)
  (->PixiGraphics2D init-opts))
