(ns rooij.interface.graphics-2d.api
  (:require [integrant.core :as ig]
            [meta-merge.core :refer [meta-merge]]))

(defn center-canvas [config]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/center-canvas? true}}))

(defn set-dimensions [config width height]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/width width
                :graphics-2d.window/height height}}))

(defn auto-scale [config]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/auto-scale true}}))

(defn on-resize [config k]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/on-resize (ig/ref k)}
               k {}}))
