(ns rooij.module.pixi.screen
  (:require
   [rooij.module.pixi.state :as state]))

(defn- scale [{:graphics-2d.window/keys [width height]}]
  (min (/ (.-innerWidth js/window) width)
       (/ (.-innerHeight js/window) height)))

(defn- update-scale [opts]
  (reset! state/scale (scale opts)))

(defn- get-x-y [{:graphics-2d.window/keys [width height]}]
  (if (and width height)
    [(* width @state/scale)
     (* height @state/scale)]
    [(.-innerWidth js/window)
     (.-innerHeight js/window)]))

(defn- get-height [view]
  (- (/ (.-innerHeight js/window) 2)
     (/ (.-height view) 2)))

(defn- get-width [view]
  (- (/ (.-innerWidth js/window) 2)
     (/ (.-width view) 2)))

(defn- center-canvas-handler [view]
  (if (> (.-innerWidth js/window) (.-width view))
    (set! (.. view -style -left) (str (get-width view) "px"))
    (set! (.. view -style -left) "0px"))
  (if (> (.-innerHeight js/window) (.-height view))
    (set! (.. view -style -top) (str (get-height view) "px"))
    (set! (.. view -style -top) "0px")))

(defn update-scene-scale []
  (doseq [[_scene-key scene] @state/scenes]
    (.. scene -scale (set @state/scale
                          @state/scale))))

(defn- resize-handler
  [{:graphics-2d.window/keys [view center-canvas?]
    :or {view "game"}
    :as opts}]
  (let [[x y] (get-x-y opts)]
    (.resize state/renderer x y)
    (when center-canvas?
      (center-canvas-handler (js/document.getElementById view)))))

(defn- handler [{:graphics-2d.window/keys [on-resize view auto-scale]
                 :as opts
                 :or {view "game"}}]
  (when auto-scale
    (update-scale opts))
  (update-scene-scale)
  (resize-handler opts)
  (on-resize (js/document.getElementById view) @state/scale))

(defn setup-event-listener-resize [opts]
  (js/setTimeout #(handler opts) 100)
  (.addEventListener js/window "resize" #(handler opts)))
