(ns rooij.loop.event
  (:require
   [rooij.system.component :as component]
   [rooij.system.entity :as entity]
   [integrant-tools.keyword :refer [ancestor?]]
   [integrant.core :as ig]))

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

(defn- get-subs [entity entities]
  (subs-states entities (-> entities entity :entity/subs)))

(defn get-component-key [scene {:event/keys [entity handler]}]
  (get-in scene [:scene/entities entity :entity/routes handler]))

(defn- get-components [scene {:event/keys [entity] :as event}]
  (->> (get-component-key scene event)
       (mapv #(get-in scene (component/path entity %)))))

(defn excludable? [{:component/keys [key]} excludes]
  (some (fn [exclude]
          (or ^boolean (identical? key exclude)
              ^boolean (ancestor? exclude key)))
        excludes))

(defn event->context [scene {:event/keys [entity handler content]} component]
  {:context/scene-key (:scene/key scene)
   :context/entity-key entity
   :context/component-key (:component/key component)
   :context/scene scene
   :context/entity (-> scene :scene/entities entity entity/state)
   :context/component component
   :context/state (:component/state component)
   :context/old-state (:component/state component)
   :context/handler (get-in component [:component/handlers handler])
   :context/handler-key handler
   :context/subs (get-subs entity (:scene/entities scene))
   :context/event content})

(defn event->contexts [scene {:event/keys [excludes] :as event}]
  (reduce
   (fn [acc component]
     (if ^boolean (excludable? component excludes)
       acc
       (conj acc (event->context scene event component))))
   []
   (get-components scene event)))
