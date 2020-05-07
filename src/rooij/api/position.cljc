(ns rooij.api.position
  (:require [rooij.api :as rooij]))

(defn set!
  "TODO"
  [context x y]
  (rooij/emit! context :general-2d.handler.position/set
               {:x x :y y :flip-x false}))
