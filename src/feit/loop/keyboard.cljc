(ns feit.loop.keyboard)

(defn process [{:scene/keys [entities]} {:input-event/keys [key type]}]
  (doseq [[_entity-key {:entity/keys [components state]}] entities
          [component-key {:component/keys [keyboards]}] components]
    (when-let [keyboard (get keyboards [type key])]
      ((:keyboard/fn keyboard) (get state component-key)))))
