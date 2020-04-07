(ns essen.interface.graphics-2d.core
  (:require
   [integrant-tools.core :as it]))

(def system
  :essen.interface.graphics-2d/system)

(def scene
  :essen.interface.graphics-2d/scene)

(it/derive-hierarchy
 {:graphics-2d.entity/asset-loader [:essen/entity]
  :graphics-2d.entity/spritesheet-loader [:essen/entity]})
