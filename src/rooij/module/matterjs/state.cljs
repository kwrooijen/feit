(ns rooij.module.matterjs.state
  (:require
   ["matter-js" :as Matter :refer [Engine World Mouse MouseConstraint]]))

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

(defn init-engine! [scene-key]
  (swap! engines assoc scene-key (.create Engine (clj->js default-world)))
  (swap! running-engines conj scene-key)
  (add-mouse-constraint! scene-key))

(defn halt-engine! [scene-key]
  (.clear Engine (get-engine scene-key))
  (swap! engines dissoc scene-key)
  (swap! running-engines disj scene-key))
