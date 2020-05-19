(ns feit.loop.reactor
  (:require
   [feit.state :as state]))

(defn- path-entity-state [{:context/keys [entity-key component-key]}]
  [:context/scene
   :scene/entities entity-key
   :entity/state component-key])

(defn- apply-reactors!
  [{:context/keys [component old-state state] :as context}]
  (doseq [[_k reactor] (:component/reactors component)]
    ((:reactor/fn reactor) context old-state state)))

(defn- save-component! [{:context/keys [entity-key component component-key state]}]
  (when ^boolean (or (:component/persistent component false)
                     (:component/auto-persistent component false))
    (state/save-component! state entity-key component-key)))

(defn process [[scene {:context/keys [old-state] :as context}] ]
  (when-not (identical? old-state (get-in scene (path-entity-state context)))
    (apply-reactors! context)
    (save-component! context))
  scene)
