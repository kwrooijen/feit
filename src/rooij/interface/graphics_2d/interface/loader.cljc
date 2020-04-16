(ns rooij.interface.graphics-2d.interface.loader
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [rooij.interface.graphics-2d.interface :refer [make-loader]]
   [rooij.state :as state]
   [rooij.query :refer [emit! transition-scene]]))

(defprotocol RooijGraphics2DLoader
  (load-spritesheet [this context file name])
  (load-texture [this context file name]))

(defn load-complete! [{:context/keys [scene-key entity-key]}]
  (emit! {:event/scene scene-key
          :event/entity entity-key
          :event/handler :graphics-2d.handler.loader/load-complete
          :event/content {}}))

(defmethod ig/prep-key :graphics-2d.component/loader [_ opts]
  (meta-merge
   {:component/handlers [{:handler/ref (ig/ref :graphics-2d.handler.loader/load-complete)}
                         {:handler/ref (ig/ref :graphics-2d.handler.loader/load-texture)}
                         {:handler/ref (ig/ref :graphics-2d.handler.loader/load-spritesheet)}]}
   opts))

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
      (if (>= (:loader/loaded new-state) to-load)
        (transition-scene scene-key next-scene)
        new-state))))
