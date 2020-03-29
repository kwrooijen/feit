(ns essen.loop.reactor
  (:require
   [essen.state :as state]))

(defn- apply-reactors!
  [{:context/keys [component old-state state] :as ctx}]
  (doseq [[_k reactor] (:component/reactors component)]
    ((:reactor/fn reactor) ctx old-state state)))

(defn- save-component! [{:context/keys [entity-key component component-key state]}]
  (when (:component/persistent component)
    (swap! state/persistent-components assoc [entity-key component-key] state)))

(defn process [{:context/keys [old-state state] :as ctx}]
  (when-not (identical? old-state state)
    (apply-reactors! ctx)
    (save-component! ctx))
  ctx)
