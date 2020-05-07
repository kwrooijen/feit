(ns rooij.interface.graphics-2d.loader
  (:require
   [integrant.core :as ig]
   [rooij.dsl :as r]
   [rooij.config]
   [rooij.interface.graphics-2d.core :refer [make-loader]]
   [rooij.api :refer [emit! transition-scene]]
   [rooij.state :as state]))

(defprotocol RooijGraphics2DLoader
  (load-spritesheet [this context file name])
  (load-texture [this context file name]))

(defn load-complete! [{:context/keys [scene-key entity-key]}]
  (emit! {:event/scene scene-key
          :event/entity entity-key
          :event/handler :graphics-2d.handler.loader/load-complete
          :event/content {}}))

(defmethod ig/init-key :graphics-2d.component/loader
  [_ {:context/keys [scene-key entity-key]
      :loader/keys [spritesheets textures next-scene]
      :as opts}]
  (doseq [spritesheet spritesheets]
    (emit! {:event/scene scene-key
            :event/entity entity-key
            :event/handler :graphics-2d.handler.loader/load-spritesheet
            :event/content spritesheet}))
  (doseq [texture textures]
    (emit! {:event/scene scene-key
            :event/entity entity-key
            :event/handler :graphics-2d.handler.loader/load-texture
            :event/content texture}))
  {:loader/loaded 0
   :loader/to-load (count (apply merge spritesheets textures))
   :loader/loader (make-loader state/graphics-2d opts)
   :loader/next-scene next-scene})

(defmethod ig/init-key :graphics-2d.handler.loader/load-spritesheet [_ _opts]
  (fn [context {:spritesheet/keys [file name]} {:loader/keys [loader] :as state}]
    (load-spritesheet loader context file name)
    state))

(defmethod ig/init-key :graphics-2d.handler.loader/load-texture [_ _opts]
  (fn [context {:texture/keys [file name]} {:loader/keys [loader] :as state}]
    (load-texture loader context file name)
    state))

(defmethod ig/init-key :graphics-2d.handler.loader/load-complete [_ _opts]
  (fn [{:context/keys [scene-key]} _event {:loader/keys [to-load next-scene] :as state}]
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
