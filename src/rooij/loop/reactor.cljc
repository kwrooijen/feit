(ns rooij.loop.reactor
  (:require
   [rooij.state :as state]))

(defn- path-entity-state [{:context/keys [entity-key component-key]}]
  [:context/scene
   :scene/entities entity-key
   :entity/state component-key])

(defn- apply-reactors!
  [{:context/keys [component old-state state] :as ctx}]
  (doseq [[_k reactor] (:component/reactors component)]
    ((:reactor/fn reactor) ctx old-state state)))

(defn- save-component! [{:context/keys [entity-key component component-key state]}]
  (when ^boolean (or (:component/persistent component false)
                     (:component/auto-persistent component false))
    (state/save-component! state entity-key component-key)))

(defn process [[scene {:context/keys [old-state] :as ctx}] ]
  (when-not ^boolean (identical? old-state (get-in scene (path-entity-state ctx)))
    (apply-reactors! ctx)
    (save-component! ctx))
  scene)
