(ns rooij.module.pixi.screen
  (:require
   [rooij.module.pixi.state :as state]))

(defn- scale [{:graphics-2d.window/keys [scale-x scale-y]}]
  (min (/ (.-innerWidth js/window) scale-x)
       (/ (.-innerHeight js/window) scale-y)))

(defn- update-scale [opts]
  (reset! state/scale (scale opts)))

(defn- get-x-y [{:graphics-2d.window/keys [scale-x scale-y]}]
  (if (and scale-x scale-y)
    [(* scale-x @state/scale)
     (* scale-y @state/scale)]
    [(.-innerWidth js/window)
     (.-innerHeight js/window)]))

(defn- get-height [view]
  (- (/ (.-innerHeight js/window) 2)
     (/ (.-height view) 2)))

(defn- get-width [view]
  (- (/ (.-innerWidth js/window) 2)
     (/ (.-width view) 2)))

(defn- center-canvas-handler [view]
  (if (> (/ (.-innerWidth js/window) (.-width view))
         (/ (.-innerHeight js/window) (.-height view)))
    (do (set! (.. view -style -left) (str (get-width view) "px"))
        (set! (.. view -style -top) "0px"))
    (do (set! (.. view -style -top) (str (get-height view) "px"))
        (set! (.. view -style -left) "0px"))))

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

(defn setup-event-listener-resize
  [{:graphics-2d.window/keys [on-resize view]
    :as opts
    :or {view "game" on-resize (fn [_])}}]
  (js/setTimeout
   (comp #(on-resize (js/document.getElementById view) @state/scale)
         #(resize-handler opts)
         #(update-scene-scale)
         #(update-scale opts))
   100)
  (.addEventListener js/window "resize"
                     (comp #(on-resize (js/document.getElementById view) @state/scale)
                           #(resize-handler opts)
                           #(update-scene-scale)
                           #(update-scale opts))))
