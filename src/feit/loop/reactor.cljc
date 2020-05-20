(ns feit.loop.reactor
  (:require
   [feit.core.state :as state]))

(defn- path-entity-state [{:context/keys [entity-key component-key]}]
  [:scene/entities entity-key
   :entity/state component-key])

(defn- apply-reactors!
  [{:context/keys [component old-state state]} time]
  (doseq [[_k reactor] (:component/reactors component)]
    ((:reactor/fn reactor) time old-state state)))

(defn- save-component! [{:context/keys [entity-key component component-key state]}]
  (when ^boolean (or (:component/persistent component false)
                     (:component/auto-persistent component false))
    (state/save-component! state entity-key component-key)))

(defn process [[scene {:context/keys [old-state] :as context}] time]
  (when-not (identical? old-state (get-in scene (path-entity-state context)))
    (apply-reactors! context time)
    (save-component! context))
  scene)
