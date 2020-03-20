(ns essen.module.matterjs
  (:require
   ["matter-js" :as Matter :refer [Engine World Mouse MouseConstraint]]
   [essen.module.matterjs.component :as matterjs.component]
   [integrant.core :as ig]
   [essen.module.matterjs.state :as state]))

(def config
  (merge matterjs.component/config))

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
        mouseConstraint (.create MouseConstraint engine
                                 #js {:mouse (.create Mouse canvas)})
        world (.-world engine)]
    (.add World world mouseConstraint)

    (.on Matter/Events engine "collisionStart"
         (fn [event]
           (let [bodyA (.-bodyA (aget (.-pairs event) 0))
                 bodyB (.-bodyB (aget (.-pairs event) 0))]
             (println "Collision BodyA" (.-label bodyA))
             (println "Collision BodyB" (.-label bodyB)))))

    ;; constant velocity
    ;; (set! (.. world -gravity -y) 0)
    ;; (set! (.. world -gravity -x) 0)
    ;; TODO Maybe create dynamic var for performance boost
    (reset! state/engine engine)
    run))
