(ns essen.loop.key
  (:require
   [integrant.core :as ig]
   [essen.state :as state :refer [input-events]]))

(defn- add-component
  [components acc component]
  (assoc acc component (get-in components [component :component/state])))

(defn- subs-states [entities subs]
  (apply merge
         (for [[key components] subs
               [derived-key opts] (ig/find-derived entities key)]
           (->> components
                (reduce (partial add-component (:entity/components opts)) {})
                (assoc {} derived-key)))))

(defn- apply-key-event
  [{:scene/keys [entities]}
   keyboard
   {:input-event/keys [tag]}]
  (let [{:keyboard/keys [subs] :as keyboard} (get keyboard tag)]
    (when-let [f (:keyboard/fn keyboard)]
      (f (subs-states entities subs)))))

(defn process [{:scene/keys [key keyboard] :as scene}]
  (swap! (get @input-events key)
         (fn [events]
           (doseq [event events]
             (apply-key-event scene keyboard event))
           [])))
