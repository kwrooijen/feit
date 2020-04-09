(ns rooij.module.box2d.core
  (:require
   [rooij.module.box2d.world :refer [bodies fixtures]]
   [rooij.module.box2d.util :refer [Vec2 Edge Circle]]
   [integrant.core :as ig]
   ["planck-js" :as planck]))

(defmethod ig/init-key :rooij.interface.physics-2d/system [_ opts]
  opts)

(defmethod ig/init-key :rooij.interface.physics-2d/scene [_ opts]
  opts)

(def config
  {:rooij.interface.physics-2d/system {}
   :rooij.interface.physics-2d/scene {}})
