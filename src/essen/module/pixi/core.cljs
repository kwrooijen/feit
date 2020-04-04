(ns essen.module.pixi.core
  (:require
   [essen.module.pixi.entity]
   [essen.module.pixi.state :as state]
   [essen.module.pixi.component.sprite :as component.sprite]
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

(defmethod ig/init-key :pixi.core/scene [_ _]
  (fn init-pixi-scene [scene-key]
    (state/init-scene! scene-key)))

(defmethod ig/halt-key! :pixi.core/scene [_ _]
  (fn halt-pixi-scene [scene-key]
    (.destroy (state/get-scene scene-key))
    (.clear state/renderer)
    (state/halt-scene! scene-key)))

(defmethod ig/init-key :pixi.core/system [_ _]
  (fn [scene-key]
    (.render state/renderer (state/get-scene scene-key))))


;; (defn js-keys->clj-keys [o]
;;   (sp/transform [MAP-VALS] clj->js (js->clj o)))

;; (defn- spritesheet-loaded
;;   [{::keys [spritesheet name transition] :context/keys [scene-key]}]
;;   (let [sheet
;;         (-> (.-shared PIXI/Loader)
;;             (.-resources)
;;             (aget spritesheet))]
;;     (swap! sheets assoc name sheet)
;;     (swap! textures assoc name (js-keys->clj-keys (.-textures sheet)))
;;     (swap! animations assoc name (js-keys->clj-keys (.-animations (.-spritesheet sheet)))))

;;   (scene/halt! scene-key)
;;   (scene/start! transition))

;; (defmethod ig/pre-init-spec ::load-spritesheet [_]
;;   (s/keys :req [::spritesheet
;;                 ::name
;;                 ::transition]))

;; (defmethod ig/init-key ::load-spritesheet [_ {::keys [spritesheet] :as opts}]
;;   (fn [_context]
;;     (-> (.-shared PIXI/Loader)
;;         (.add spritesheet)
;;         (.load (partial spritesheet-loaded opts)))))

(def config
  (merge
   {[:essen.interface.graphics-2d/system :pixi.core/system]
    {:dep/renderer (ig/ref :pixi.core/renderer)
     :dep/event-listener.resize (ig/ref :pixi.core.event-listener/resize)}

    [:essen.interface.graphics-2d/scene :pixi.core/scene] {}

    :pixi.core.event-listener/resize {}

    [:essen.interface.graphics-2d/window :pixi.core/renderer] {}}
   component.sprite/config))
