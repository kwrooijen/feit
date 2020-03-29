(ns essen.loop.handler
  (:require))

(defn path-entity-state [{:context/keys [entity-key component-key]}]
  [:context/scene
   :scene/entities entity-key
   :entity/components component-key
   :component/state])

(defn process [{:context/keys [handler event state] :as ctx}]
  (let [state ((:handler/fn handler) ctx event state)]
    (-> ctx
        (assoc-in (path-entity-state ctx) state)
        (assoc :context/state state))))
