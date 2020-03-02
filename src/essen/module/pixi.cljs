(ns essen.module.pixi
  (:require
   [essen.state :refer [state]]
   [essen.core :as essen]
   [essen.entity]
   [essen.loop]
   [integrant.core :as ig]
   ["pixi.js" :as PIXI :refer [Renderer Texture Container Sprite Ticker]]))

(defn renderer []
  (-> @state :pixi :pixi/renderer))

(defn path-container [stage-key]
  [:pixi :pixi/stage stage-key :stage/container])

(defn container [stage-key]
  (get-in @state (path-container stage-key)))

(defn path-ticker [stage-key]
  [:pixi :pixi/stage stage-key :stage/ticker])

(defn ticker [stage-key]
  (get-in @state (path-ticker stage-key)))

(defn setup-renderer
  [{:renderer/keys [view width height resolution auto-dencity]
    :or {view        "game"
         width       (.-innerWidth js/window)
         height      (.-innerHeight js/window)
         resolution  (.-devicePixelRatio js/window)
         auto-dencity true}}]
  (swap! state assoc-in [:pixi :pixi/renderer]
         (Renderer.
          #js{:view        (js/document.getElementById view)
              :width       width
              :height      height
              ;; For some reason this doubles screen width
              ;; :resolution  resolution
              :autoDencity auto-dencity})))

(defn setup-stage [stage-key]
  (swap! state assoc-in [:pixi :pixi/stage stage-key :stage/container]
         (Container.))
  (swap! state assoc-in [:pixi :pixi/stage stage-key :stage/ticker]
         (Ticker.)))

(defn handler-resize []
  (.resize (renderer)
           (.-innerWidth js/window)
           (.-innerHeight js/window)))

(defn animate [stage-state stage-key delta]
  ;; TODO handle swapping within essen.loop/run, and only pass in the key
  (swap! stage-state essen.loop/run delta (.now js/Date))
  (.render (renderer) (container stage-key)))

(defn start-loop [stage-key]
  (let [scene-state (essen/get-scene stage-key)]
    (-> @state
        (get-in [:pixi :pixi/stage stage-key :stage/ticker])
        (.add (partial animate scene-state stage-key))
        (.start))))

(defn stop-loop []
  (-> (:ticker @state)
      (.remove (partial animate))
      (.stop)))

(defn setup [{:game/keys [renderer stages]}]
  (.addEventListener js/window "resize" handler-resize)
  (setup-renderer renderer)
  (swap! state assoc-in [:pixi :pixi/stage-config] stages)
  (swap! state assoc-in [:pixi :pixi/running-stages] #{}))

(defmethod ig/init-key :stage/name [_ opts] opts)

(defn stage-start [config stage-key]
  ;; (when (get-in @state [:essen/scenes stage-key])
  ;;   (throw (js/Error. (str "Stage: " stage-key " is already running."))))

  ;; (when-not (get-in @state [:pixi :pixi/stage-config stage-key])
  ;;   (throw (js/Error. (str "Stage: " stage-key " does not exist."))))

  (setup-stage stage-key)
  (essen.entity/init-scene config stage-key)
  (swap! state update-in [:pixi :pixi/running-stages] conj stage-key)
  (start-loop stage-key))

(defn stage-stop [stage-key]
  (.destroy (ticker stage-key))
  (.destroy (container stage-key))
  (.clear (renderer))
  (swap! state update-in [:pixi :pixi/stage] dissoc stage-key)
  (swap! state update-in [:pixi :pixi/running-stages] disj stage-key))
