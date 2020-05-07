(ns rooij.api.sprite
  (:require [rooij.api :as rooij]))

(defn flip! [context flip-x flip-y]
  (rooij/emit! context :graphics-2d.handler.sprite/flip
               {:event/x flip-x :event/y flip-y}))
