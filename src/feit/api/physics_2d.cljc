(ns feit.api.physics-2d
  (:require
   [feit.api :as r]))

(defn set-velocity-x
  "TODO"
  [context x]
  (r/emit! context :physics-2d.handler.shape/set-velocity-x x))

(defn set-velocity-y
  "TODO"
  [context y]
  (r/emit! context :physics-2d.handler.shape/set-velocity-y y))

(defn set-velocity
  "TODO"
  [context x y]
  (r/emit! context :physics-2d.handler.shape/set-velocity [x y]))

(defn add-velocity
  "TODO"
  [context x y]
  (r/emit! context :physics-2d.handler.shape/add-velocity [x y]))
