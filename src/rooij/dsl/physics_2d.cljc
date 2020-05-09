(ns rooij.dsl.physics-2d
  (:require [meta-merge.core :refer [meta-merge]]
            [rooij.dsl :as r]))

(defn- merge-component [config m]
  (update-in config (:component/last (meta config)) merge m))

(defn- meta-merge-component [config m]
  (update-in config (:component/last (meta config)) meta-merge m))

(defn component-rectangle
  "TODO"
  ([config k x y w h]
   (r/ref-component config [:physics-2d.component/rectangle k] {:x x :y y :w w :h h})))

(defn static?
  "TODO"
  [config boolean]
  (merge-component config {:static? boolean}))

(defn angle
  "TODO"
  [config angle]
  (merge-component config {:angle angle}))

(defn set-position
  "TODO"
  [config x y]
  (merge-component config {:x x :y y}))
