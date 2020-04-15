(ns rooij.module.matterjs.debug
  (:require
   [rooij.module.matterjs.state :as state]))

(defn vertex->point [vertex]
  [(.-x vertex)
   (.-y vertex)])

(defn body->points [body]
  (.map (.-vertices body) vertex->point))

(defn engine->points [engine]
  (.map (.-bodies (.-world engine)) body->points))

(defn wireframe-vectors [scene-key]
  (js->clj (engine->points (state/get-engine scene-key))))
