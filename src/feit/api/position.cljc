(ns feit.api.position
  (:require [feit.api :as feit]))

(defn set!
  "TODO"
  [context x y]
  (feit/emit! context :general-2d.handler.position/set
               {:x x :y y :flip-x false}))
