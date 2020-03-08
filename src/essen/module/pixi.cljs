(ns essen.module.pixi
  (:require
   [essen.module.pixi.state :refer [state]]
   [essen.loop]
   [integrant.core :as ig]
   ["pixi.js" :as PIXI :refer [Renderer Texture Container Sprite Ticker]]))

(defn renderer []
  (:pixi/renderer @state))

(defn path-container [stage-key]
  [:pixi/stage stage-key :stage/container])

(defn container [stage-key]
  (get-in @state (path-container stage-key)))

(defn path-ticker [stage-key]
  [:pixi/stage stage-key :stage/ticker])

(defn ticker [stage-key]
  (get-in @state (path-ticker stage-key)))

(defn setup-renderer
  [{:renderer/keys [view width height resolution auto-dencity]
    :or {view        "game"
         width       (.-innerWidth js/window)
         height      (.-innerHeight js/window)
         resolution  (.-devicePixelRatio js/window)
         auto-dencity true}}]
  (swap! state assoc :pixi/renderer
         (Renderer.
          #js{:view        (js/document.getElementById view)
              :width       width
              :height      height
              ;; For some reason this doubles screen width
              ;; :resolution  resolution
              :autoDencity auto-dencity})))

(defn setup-stage [stage-key]
  (swap! state assoc-in (path-container stage-key)
         (Container.))
  (swap! state assoc-in (path-ticker stage-key)
         (Ticker.)))

(defn handler-resize []
  (.resize (renderer)
           (.-innerWidth js/window)
           (.-innerHeight js/window)))

(defn animate [stage-key delta]
  (essen.loop/run stage-key delta (.now js/Date))
  (.render (renderer) (container stage-key)))

(defn start-loop [stage-key]
  (-> (ticker stage-key)
      (.add (partial animate stage-key))
      (.start)))

(defn stop-loop [stage-key]
  (-> (ticker stage-key)
      (.remove (partial animate))
      (.stop)))

(defn setup [{:game/keys [renderer stages]}]
  (.addEventListener js/window "resize" handler-resize)
  (setup-renderer renderer)
  (swap! state assoc-in [:pixi/stage-config] stages)
  (swap! state assoc-in [:pixi/running-stages] #{}))

(defmethod ig/init-key :stage/name [_ opts] opts)

(defn stage-start [config stage-key]
  (setup-stage stage-key)
  (swap! state update-in [:pixi/running-stages] conj stage-key)
  (start-loop stage-key))

(defn stage-stop [_config stage-key]
  (.destroy (ticker stage-key))
  (.destroy (container stage-key))
  (.clear (renderer))
  (swap! state update-in [:pixi/stage] dissoc stage-key)
  (swap! state update-in [:pixi/running-stages] disj stage-key))

(defn stage-suspend [config stage-key])

(def module
  {:essen/setup setup
   :essen/stage-start stage-start
   :essen/stage-stop stage-stop
   :essen/stage-suspend stage-suspend})
