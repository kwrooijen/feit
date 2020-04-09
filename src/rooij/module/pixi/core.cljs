(ns rooij.module.pixi.core
  (:require
   [rooij.module.pixi.entity]
   [rooij.module.pixi.state :as state]
   [rooij.module.pixi.component.sprite :as component.sprite]
   [integrant.core :as ig]))

(defmethod ig/init-key :pixi.core.event-listener/resize [_ opts]
  ;; TODO trigger handler
  ;; (js/setTimeout #(handler-resize) 100)
  (.addEventListener js/window "resize"
                     #(.resize state/renderer
                               (.-innerWidth js/window)
                               (.-innerHeight js/window))))

(defmethod ig/init-key :pixi.core/renderer
  [_ {:graphics-2d.window/keys [view width height auto-dencity]
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

(defmethod ig/init-key :rooij.interface.graphics-2d/scene  [_ _]
  (fn init-pixi-scene [scene-key]
    (state/init-scene! scene-key)))

(defmethod ig/halt-key! :rooij.interface.graphics-2d/scene [_ _]
  (fn halt-pixi-scene [scene-key]
    (.destroy (state/get-scene scene-key))
    (.clear state/renderer)
    (state/halt-scene! scene-key)))

(defmethod ig/init-key :rooij.interface.graphics-2d/system [_ _]
  (fn [scene-key]
    (.render state/renderer (state/get-scene scene-key))))

(def core-config
  {:rooij.interface.graphics-2d/system
   {:dep/renderer (ig/ref :pixi.core/renderer)
    :dep/event-listener.resize (ig/ref :pixi.core.event-listener/resize)}

   :rooij.interface.graphics-2d/scene {}

   :pixi.core.event-listener/resize {}

   :pixi.core/renderer {}})

(def config
  (merge
   core-config
   component.sprite/config))
