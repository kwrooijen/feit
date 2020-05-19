(ns feit.module.matterjs.core
  (:require
   [integrant.core :as ig]
   [feit.module.matterjs.interface :refer [->MatterPhysics2D]]))

(defmethod ig/init-key :feit.interface.physics-2d/system [_ init-opts]
  (->MatterPhysics2D init-opts))
