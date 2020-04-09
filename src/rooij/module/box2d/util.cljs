(ns rooij.module.box2d.util
  (:require
   ["planck-js" :as planck]))


(def Vec2 (.-Vec2 planck))

(def Edge (.-Edge planck))

(def Circle (.-Circle planck))
