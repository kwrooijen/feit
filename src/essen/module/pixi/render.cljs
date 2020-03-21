(ns essen.module.pixi.render
  (:require
   ["pixi.js" :as PIXI :refer [Renderer Container Ticker]]
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
  [{:renderer/keys [view width height resolution auto-dencity]
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
              ;; For some reason this doubles screen width
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

(defn setup [{:game/keys [renderer stages]}]
  (.addEventListener js/window "resize" handler-resize)
  (setup-renderer renderer)
  (js/setTimeout #(handler-resize) 100)
  (swap! state assoc-in [:pixi/stage-config] stages)
  (swap! state assoc-in [:pixi/running-stages] #{}))

(defn stage-start [config stage-key]
  (start-loop stage-key)
  (setup-stage stage-key)
  (swap! state update-in [:pixi/running-stages] conj stage-key))

(defn stage-halt [_config stage-key]
  (-> (:pixi/ticker @state)
      (.remove (get @tickers stage-key)))
  (.destroy (container stage-key))
  (.clear (renderer))
  (swap! state update-in [:pixi/stage] dissoc stage-key)
  (swap! state update-in [:pixi/running-stages] disj stage-key))

(defn stage-suspend [config stage-key])

(defn stage-resume [config stage-key])
