(ns rooij.loop.keyboard
  (:require
   [integrant.core :as ig]
   [integrant-tools.core :as it]))

(defn- add-component
  [components acc component-key]
  (->> (get (it/find-derived-value components component-key) :component/state)
       (assoc acc component-key)))

(defn- subs-states [{:scene/keys [entities]} {:keyboard/keys [subs]}]
  (apply merge
         {}
         (for [[key components] subs
               [derived-key opts] (ig/find-derived entities key)]
           (->> components
                (reduce (partial add-component (:entity/components opts)) {})
                (assoc {} derived-key)))))

(defn process [{:context/keys [scene] :as context} {:input-event/keys [key type]}]
  (when-let [keyboard-key (get-in scene [:scene/keyboard [type key]])]
    (-> context
        (assoc :context/subs (subs-states scene keyboard-key))
        ((:keyboard/fn keyboard-key)))))
