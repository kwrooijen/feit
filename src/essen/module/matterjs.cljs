(ns essen.module.matterjs
  (:require
   ["matter-js" :as MatterJs :refer [Engine Render Bodies World Mouse MouseConstraint Composite]]
   [integrant.core :as ig]))

;; Remove a body
;; (.remove Composite (.-world engine) boxA)
(defonce box (atom nil))
(defonce points (atom []))
(defonce engine1 (atom nil))
(defonce render1 (atom nil))

(defonce mouse-constraint (atom nil))

(defn vertex->point [vertex]
  {:x (.-x vertex) :y (.-y vertex)})

(defn body->points [body]
  (.map (.-vertices body) vertex->point))

(defn engine->points [engine]
  (.map (.-bodies (.-world engine)) body->points))

(defn run [delta]
  (when @engine1
    (.update Engine @engine1 (/ 1000 60) 1)
    (reset! points (js->clj (engine->points @engine1)))))

(defmethod ig/init-key :matterjs/start [_ opts]
  (println "Creating engineeeee")
  (let [canvas (.getElementById js/document "game")
        engine (.create Engine (clj->js {:render {:canvas canvas
                                                  :width (.-width canvas)
                                                  :height (.-height canvas)}}))
        boxA (.rectangle Bodies 400 200 20 31)
        ground (.rectangle Bodies 400 610 810 60 (clj->js {:isStatic true
                                                           :restitution 1}))
        mouseConstraint (.create MouseConstraint engine
                                 (js->clj {:mouse (.create Mouse canvas)}))]
    (.add World (.-world engine) ground)
    (.add World (.-world engine) boxA)
    (.add World (.-world engine) mouseConstraint)
    (reset! box boxA)
    (reset! engine1 engine)
    run))
