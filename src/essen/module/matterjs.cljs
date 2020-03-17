(ns essen.module.matterjs
  (:require
   ["matter-js" :as Matter :refer [Engine Render Bodies World Mouse MouseConstraint Composite]]
   [integrant.core :as ig]))

;; Remove a body
;; (.remove Composite (.-world engine) boxA)
(defonce box (atom nil))
(defonce points (atom []))
(defonce engine1 (atom nil))
(defonce render1 (atom nil))

(defonce mouse-constraint (atom nil))

(defn vertex->point [vertex]
  {:x (.-x vertex)
   :y (.-y vertex)})

(defn body->points [body]
  (.map (.-vertices body) vertex->point))

(defn engine->points [engine]
  (.map (.-bodies (.-world engine)) body->points))

(defn run [delta]
  (when @engine1
    (.update Engine @engine1 (/ 1000 60) 1)
    (reset! points (js->clj (engine->points @engine1)))))

(defmethod ig/init-key :matterjs/start [_ opts]
  (let [canvas (.getElementById js/document "game")
        engine (.create Engine (clj->js {:render {:canvas canvas
                                                  :width (.-width canvas)
                                                  :height (.-height canvas)}}))
        boxA (.rectangle Bodies 400 200 20 31 #js {:restitution 1})
        circleA (.circle Bodies 400 200 100 #js {:restitution 0.4 :label "Some Circle"})
        ground (.rectangle Bodies 400 610 810 60 (clj->js {:isStatic true
                                                           :restitution 1}))
        mouseConstraint (.create MouseConstraint engine
                                 (js->clj {:mouse (.create Mouse canvas)}))]
    (.add World (.-world engine) boxA)
    (.add World (.-world engine) circleA)
    (.add World (.-world engine) ground)
    (.add World (.-world engine) mouseConstraint)

    (.on Matter/Events engine "collisionStart"
         (fn [event]
           (let [bodyA (.-bodyA (aget (.-pairs event) 0))
                 bodyB (.-bodyB (aget (.-pairs event) 0))]
             (println (.-label bodyA))
             (.setVelocity Matter/Body bodyA #js{:x 0 :y -10}))))

    ;; constant velocity
    ;; (set! (.-restitution circleA) 1)
    ;; (set! (.-friction circleA) 0)
    ;; (set! (.-frictionAir circleA) 0)
    ;; (set! (.. engine -world -gravity -y) 0)
    ;; (set! (.. engine -world -gravity -x) 0)

    (reset! box boxA)
    (reset! engine1 engine)
    run))
