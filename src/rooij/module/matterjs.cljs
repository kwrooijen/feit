(ns rooij.module.matterjs
  (:require
   ["matter-js" :as Matter :refer [Engine World Mouse MouseConstraint]]
   [rooij.module.matterjs.component :as matterjs.component]
   [rooij.module.matterjs.state :as state]
   [rooij.util :refer [ns-map->nested-map]]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]))

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

(derive :rooij.module/matterjs :rooij.module/physics)
(derive :rooij.module.spawn/matterjs :rooij.module.spawn/physics)

(def default-render
  (let [canvas (.getElementById js/document "game")]
    {:render {:canvas canvas
              :width (.-width canvas)
              :height (.-height canvas)}}))

(def ^{:doc "Default opts for the MatterJS world.
 https://github.com/liabru/matter-js/issues/129 states that the `Engine.create`
 method should automatically add these if they are missing. But for some reason
 that doesn't work."}
  default-world
  {:world
   {:id 0
    :type "composite"
    :parent nil
    :isModified false
    :bodies []
    :constraints []
    :composites []
    :label "World"
    :plugin {}
    :gravity {:x 0
              :y 1
              :scale 0.001}
    :bounds {:min {:x ##-Inf
                   :y ##-Inf}
             :max {:x ##Inf
                   :y ##Inf}}}})

(defn render-config [opts]
  (select-keys (meta-merge default-render (get opts :engine))
               [:render]))

(defn world-config [opts]
  (-> default-world
      (meta-merge (get opts :engine))
      (select-keys [:world])))

(defmethod ig/init-key :rooij.module/matterjs [_ opts]
  (let [nested-opts (ns-map->nested-map opts)
        engine (.create Engine (clj->js
                                (merge (render-config nested-opts)
                                       (world-config nested-opts))))
        mouse (.create Mouse (.. engine -render -canvas))
        mouseConstraint (.create MouseConstraint engine #js {:mouse mouse})
        world (.-world engine)]
    (.add World world mouseConstraint)

    (.on Matter/Events engine "collisionStart"
         (fn [event]
           (let [bodyA (.-bodyA (aget (.-pairs event) 0))
                 bodyB (.-bodyB (aget (.-pairs event) 0))]
             ;; (println "Collision BodyA" (.-label bodyA))
             ;; (println "Collision BodyB" (.-label bodyB))
             )))

    ;; TODO Maybe create dynamic var for performance boost
    (reset! state/engine engine)
    run))
