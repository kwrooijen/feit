(ns rooij.interface.graphics-2d.api
  (:require [integrant.core :as ig]
            [meta-merge.core :refer [meta-merge]]))

(defn center-canvas [config]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/center-canvas? true}}))

(defn set-scale [config x y]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/scale-x x
                :graphics-2d.window/scale-y y}}))

(defn on-resize [config k]
  (meta-merge config
              {:rooij.interface.graphics-2d/system
               {:graphics-2d.window/on-resize (ig/ref k)}
               k {}}))
