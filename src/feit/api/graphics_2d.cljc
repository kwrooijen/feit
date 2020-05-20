(ns feit.api.graphics-2d
  (:require
   [feit.api :as api]))

(defn set-position!
  "TODO"
  ([context x y]
   (set-position! context x y {}))
  ([context x y opts]
   (api/emit! context :general-2d.handler.position/set
              (merge {:x x :y y} opts))))
