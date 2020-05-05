(ns rooij.module.pixi.core
  (:require [integrant.core :as ig]
            [meta-merge.core :refer [meta-merge]]
            [rooij.module.pixi.interface :refer [->PixiGraphics2D]]
            [rooij.module.pixi.interface.loader]
            [rooij.module.pixi.state :as state]
            [rooij.module.pixi.screen :as pixi.screen]))

(defn center-canvas [config]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/center-canvas? true}}))

(defn set-scale [config x y]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/scale-x x
                :graphics-2d.window/scale-y y}}))

(defn on-resize [config k]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/on-resize (ig/ref k)}
               k {}}))

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
