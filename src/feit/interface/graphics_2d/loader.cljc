(ns feit.interface.graphics-2d.loader
  (:require
   [integrant.core :as ig]
   [feit.dsl :as r]
   [feit.config]
   [feit.interface.graphics-2d.core :refer [make-loader]]
   [feit.api :refer [emit! transition-scene]]
   [feit.core.state :as state]))

(defprotocol FeitGraphics2DLoader
  (load-spritesheet [this context file name])
  (load-texture [this context file name]))

(defn load-complete! [{:context/keys [scene-key entity-key]}]
  (emit! {:context/scene-key scene-key
          :context/entity-key entity-key}
         :graphics-2d.handler.loader/load-complete))

(defmethod ig/init-key :graphics-2d.component/loader
  [_ {:context/keys [scene-key entity-key]
      :loader/keys [spritesheets textures next-scene]
      :as opts}]
  (doseq [spritesheet spritesheets]
    (emit! {:context/scene-key scene-key
            :context/entity-key entity-key}
           :graphics-2d.handler.loader/load-spritesheet
           spritesheet))
  (doseq [texture textures]
    (emit! {:context/scene-key scene-key
            :context/entity-key entity-key}
           :graphics-2d.handler.loader/load-texture
           texture))
  {:loader/loaded 0
   :loader/to-load (count (apply merge spritesheets textures))
   :loader/loader (make-loader state/graphics-2d opts)
   :loader/next-scene next-scene})

(defmethod ig/init-key :graphics-2d.handler.loader/load-spritesheet [_ opts]
  (fn [_context {:spritesheet/keys [file name]} {:loader/keys [loader] :as state}]
    (load-spritesheet loader opts file name)
    state))

(defmethod ig/init-key :graphics-2d.handler.loader/load-texture [_ opts]
  (fn [_context {:texture/keys [file name]} {:loader/keys [loader] :as state}]
    (load-texture loader opts file name)
    state))

(defmethod ig/init-key :graphics-2d.handler.loader/load-complete [_ {:context/keys [scene-key]}]
  (fn [_context _event {:loader/keys [to-load next-scene] :as state}]
    (let [new-state (update state :loader/loaded inc)]
      (if (and next-scene
               (>= (:loader/loaded new-state) to-load))
        (transition-scene scene-key next-scene)
        new-state))))

(-> (r/handler :graphics-2d.handler.loader/load-complete)
    (r/handler :graphics-2d.handler.loader/load-texture)
    (r/handler :graphics-2d.handler.loader/load-spritesheet)
    (r/component :graphics-2d.component/loader)
    (r/ref-handler :graphics-2d.handler.loader/load-complete)
    (r/ref-handler :graphics-2d.handler.loader/load-texture)
    (r/ref-handler :graphics-2d.handler.loader/load-spritesheet)
    (r/save-interface!))
