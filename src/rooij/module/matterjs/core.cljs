(ns rooij.module.matterjs.core
  (:require
   [integrant.core :as ig]
   [rooij.module.matterjs.interface :refer [->MatterPhysics2D]]))

(defmethod ig/init-key :rooij.interface.physics-2d/system [_ init-opts]
  (->MatterPhysics2D init-opts))
