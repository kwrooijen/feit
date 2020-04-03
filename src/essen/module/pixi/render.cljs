(ns essen.module.pixi.render
  (:require
   ["pixi.js" :as PIXI :refer [Renderer Container]]
   [integrant.core :as ig]
   [essen.module.pixi.state :refer [state]]))


(defn renderer []
  (:pixi/renderer @state))

(defn path-container [stage-key]
  [:pixi/stage stage-key :stage/container])

(defn container [stage-key]
  (get-in @state (path-container stage-key)))

(defn setup-renderer
  [{:pixi.renderer/keys [view width height resolution auto-dencity]
    :or {view        "app"
         width       (.-innerWidth js/window)
         height      (.-innerHeight js/window)
         resolution  (.-devicePixelRatio js/window)
         auto-dencity true}}]
  (swap! state assoc :pixi/renderer
         (Renderer.
          #js{:view        (js/document.getElementById view)
              :width       width
              :height      height
              :transparent true
              ;; For some reason this resolution doubles screen width
              ;; :resolution  resolution
              :autoDencity auto-dencity})))

(defn setup-stage [stage-key]
  (swap! state assoc-in (path-container stage-key) (Container.)))

(defn handler-resize []
  (.resize (renderer)
           (.-innerWidth js/window)
           (.-innerHeight js/window)))

(defn render [stage-key]
  (.render (renderer) (container stage-key)))

(defmethod ig/init-key :essen.module/pixi [_ opts]
  (.addEventListener js/window "resize" handler-resize)
  (setup-renderer opts)
  (js/setTimeout #(handler-resize) 100)
  (swap! state assoc-in [:pixi/running-stages] #{}))

(defmethod ig/init-key :essen.module.spawn/pixi [_ {:essen/keys [scene]}]
  (setup-stage scene)
  (swap! state update-in [:pixi/running-stages] conj scene))

(defmethod ig/halt-key! :essen.module.spawn/pixi [_ {:essen/keys [scene]}]
  (.destroy (container scene))
  (.clear (renderer))
  (swap! state update-in [:pixi/stage] dissoc scene)
  (swap! state update-in [:pixi/running-stages] disj scene))

(defmethod ig/suspend-key! :essen.module.spawn/pixi [_ opts]
  opts)

(defmethod ig/resume-key :essen.module.spawn/pixi [key opts old-opts old-impl]
  opts)
