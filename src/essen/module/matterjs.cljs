(ns essen.module.matterjs
  (:require
   ["matter-js" :as Matter :refer [Engine Render Bodies World Mouse MouseConstraint Composite]]
   [essen.module.matterjs.component.rectangle]
   [integrant.core :as ig]
   [essen.module.matterjs.state :as state]))

(def config {})

(defonce mouse-constraint (atom nil))

(defn vertex->point [vertex]
  {:x (.-x vertex)
   :y (.-y vertex)})

(defn body->points [body]
  (.map (.-vertices body) vertex->point))

(defn engine->points [engine]
  (.map (.-bodies (.-world engine)) body->points))

(defn points []
  (js->clj (engine->points @state/engine)))

(defn run [_delta]
  (.update Engine @state/engine (/ 1000 60) 1))

(defmethod ig/init-key :matterjs/start [_ opts]
  (let [canvas (.getElementById js/document "game")
        engine  (.create Engine (clj->js {:render {:canvas canvas
                                                    :width (.-width canvas)
                                                    :height (.-height canvas)}}))
        circleA (.circle Bodies 400 200 100 #js {:restitution 0.4 :label "Some Circle"})
        ground (.rectangle Bodies 400 610 810 60 (clj->js {:isStatic true
                                                           :restitution 1}))
        mouseConstraint (.create MouseConstraint engine
                                 (js->clj {:mouse (.create Mouse canvas)}))
        world (.-world engine)]
    (.add World world circleA)
    (.add World world ground)
    (.add World world mouseConstraint)

    (.on Matter/Events engine "collisionStart"
         (fn [event]
           (let [bodyA (.-bodyA (aget (.-pairs event) 0))
                 bodyB (.-bodyB (aget (.-pairs event) 0))]
             ;; (println (.-label bodyA))
             ;; (.setVelocity Matter/Body bodyA #js{:x 0 :y -10})
             ;;
             )))

    ;; constant velocity
    ;; (set! (.-restitution circleA) 1)
    ;; (set! (.-friction circleA) 0)
    ;; (set! (.-frictionAir circleA) 0)
    ;; (set! (.. world -gravity -y) 0)
    ;; (set! (.. world -gravity -x) 0)

    (reset! state/engine engine)
    run))
