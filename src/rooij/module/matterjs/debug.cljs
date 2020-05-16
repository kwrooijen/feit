(ns rooij.module.matterjs.debug
  (:require
   [rooij.module.matterjs.state :as state]))

(defn vertex->point [vertex]
  [(.-x vertex)
   (.-y vertex)])

(defn part->points
  [part]
  {:points (.map (.-vertices part) vertex->point)})

(defn body->points [body]
  {:body
   (-> (.-parts body)
       (.map part->points))})

(defn engine->points [engine]
  (-> engine
      (.-world)
      (.-bodies)
      (.map body->points)
      (.flat)))

(defn wireframe-vectors [scene-key]
  (when-let [engine (state/get-engine scene-key)]
    (->> (engine->points engine)
         (js->clj)
         (mapcat (comp rest :body))
         (mapv :points))))
