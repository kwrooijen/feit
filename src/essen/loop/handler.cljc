(ns essen.loop.handler)

(defn- path-entity-state [{:context/keys [entity-key component-key]}]
  [:scene/entities entity-key
   :entity/components component-key
   :component/state])

(defn process [[scene {:context/keys [handler event state] :as context}]]
  (let [state ((:handler/fn handler) context event state)]
    [(assoc-in scene (path-entity-state context) state) context]))
