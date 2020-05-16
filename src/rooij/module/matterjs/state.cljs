(ns rooij.module.matterjs.state
  (:require
   ["matter-js" :as Matter :refer [Engine Events Mouse MouseConstraint World]]
   [rooij.api :as api]))

(defonce ^:private engines (atom nil))

(defonce ^:private running-engines (atom #{}))

(defn get-engine [scene-key]
  (get @engines scene-key))

(defn get-world [scene-key]
  (.-world (get @engines scene-key)))

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
    :bounds {:min {:x ##-Inf :y ##-Inf}
             :max {:x ##Inf :y ##Inf}}}})

(defn add-mouse-constraint! [scene-key]
  (let [engine (get-engine scene-key)
        mouse (.create Mouse (js/document.getElementById "game"))
        mouseConstraint (.create MouseConstraint engine #js {:mouse mouse})]
    (.add World (.-world engine) mouseConstraint)))

(defn ^boolean component-collision? [body-a body-b]
  (some?
   (when-let [target (.-collision-target body-a)]
     (or (#{target} (.-collision-key body-b))
         ((ancestors (.-collision-key body-b)) target)))))

(defn add-collision!
  ""
  [scene-key]
  (.on Events (get-engine scene-key) "collisionStart"
       (fn [event]
         (let [pairs (-> (.-pairs event) (aget 0))
               body-a (.-bodyA pairs)
               body-b (.-bodyB pairs)]
           (when (component-collision? body-a body-b)
             (api/emit! (.-context body-a) (.-collision-handler body-a)))
           (when (component-collision? body-b body-a)
             (api/emit! (.-context body-b) (.-collision-handler body-b)))))))

(defn init-engine! [scene-key]
  (swap! engines assoc scene-key (.create Engine (clj->js default-world)))
  (swap! running-engines conj scene-key)
  (add-mouse-constraint! scene-key)
  (add-collision! scene-key))

(defn halt-engine! [scene-key]
  (.clear Engine (get-engine scene-key))
  (swap! engines dissoc scene-key)
  (swap! running-engines disj scene-key))
