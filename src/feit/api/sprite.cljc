(ns feit.api.sprite
  (:require [feit.api :as feit]))

(defn flip! [context flip-x flip-y]
  (feit/emit! context :graphics-2d.handler.sprite/flip
               {:event/x flip-x :event/y flip-y}))
