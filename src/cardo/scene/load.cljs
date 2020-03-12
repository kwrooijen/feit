(ns cardo.scene.load
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :scene/load [_ opts]
  opts)

(def config
  {[:essen/scene :scene/load]
   {:pixi (ig/ref :essen.module.pixi/load-spritesheet)}

   :essen.module.pixi/load-spritesheet
   {:essen.module.pixi/transition :scene/start
    :essen.module.pixi/name :spritesheet
    :essen.module.pixi/spritesheet "images/spritesheet.json"}})
