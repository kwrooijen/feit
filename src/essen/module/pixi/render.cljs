(ns essen.module.pixi.render
  (:require
   ["pixi.js" :as PIXI :refer [Renderer Container Ticker]]
   [integrant.core :as ig]
   [essen.loop]
   [essen.module.pixi.state :refer [state]]))

(defonce tickers (atom {}))

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
  (swap! state assoc :pixi/ticker (Ticker.))
  (.start (:pixi/ticker @state))
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

(defn animate [stage-key delta]
  (essen.loop/run stage-key delta (.now js/Date))
  (.render (renderer) (container stage-key)))

(defn add-ticker [f k]
  (swap! tickers assoc k f)
  (-> (:pixi/ticker @state)
      (.add f)))

(defn start-loop [stage-key]
  (add-ticker (partial animate stage-key) stage-key))

(defmethod ig/init-key :essen.module/pixi [_ opts]
  (.addEventListener js/window "resize" handler-resize)
  (setup-renderer opts)
  (js/setTimeout #(handler-resize) 100)
  (swap! state assoc-in [:pixi/running-stages] #{}))

(defmethod ig/init-key :essen.module.spawn/pixi [_ {:essen/keys [scene]}]
  (start-loop scene)
  (setup-stage scene)
  (swap! state update-in [:pixi/running-stages] conj scene))

(defmethod ig/halt-key! :essen.module.spawn/pixi [_ {:essen/keys [scene]}]
  (-> (:pixi/ticker @state)
      (.remove (get @tickers scene)))
  (.destroy (container scene))
  (.clear (renderer))
  (swap! state update-in [:pixi/stage] dissoc scene)
  (swap! state update-in [:pixi/running-stages] disj scene))

(defmethod ig/suspend-key! :essen.module.spawn/pixi [_ opts]
  opts)

(defmethod ig/resume-key :essen.module.spawn/pixi [key opts old-opts old-impl]
  opts)
