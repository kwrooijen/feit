(ns rooij.module.box2d.core
  (:require
   [rooij.module.box2d.state :as state]
   [rooij.module.box2d.component]
   [rooij.module.box2d.world]
   [integrant.core :as ig]))

(defmethod ig/init-key :rooij.interface.physics-2d/scene [_ _opts]
  (fn interface-physics-2d--scene-init  [scene-key]
    (state/init-world! scene-key)))

(defmethod ig/halt-key! :rooij.interface.physics-2d/scene [_ _opts]
  (fn interface-physics-2d--scene-halt! [scene-key]
    (state/halt-world! scene-key)))

(defmethod ig/init-key :rooij.interface.physics-2d/system [_ _opts]
  (fn [delta scene-key]
    (-> scene-key
        (state/get-world)
        (.step delta))))

(def config
  {:rooij.interface.physics-2d/system {}
   :rooij.interface.physics-2d/scene {}})
