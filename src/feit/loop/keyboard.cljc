(ns feit.loop.keyboard)

(defn process [{:scene/keys [entities]} {:input-event/keys [key type]} time]
  (doseq [[_entity-key {:entity/keys [components state]}] entities
          [component-key {:component/keys [keyboards]}] components]
    (when-let [keyboard (get keyboards [type key])]
      ((:keyboard/fn keyboard) time (get state component-key)))))
