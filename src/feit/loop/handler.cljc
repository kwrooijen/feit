(ns feit.loop.handler)

(defn process
  [[scene {:context/keys [handler event state entity-key component-key] :as context}]]
  (let [new-state ((:handler/fn handler) context event state)]
    [(assoc-in scene [:scene/entities entity-key :entity/state component-key] new-state)
     (assoc context :context/state new-state)]))
