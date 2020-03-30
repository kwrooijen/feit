(ns essen.loop.event
  (:require
   [essen.system.component :as component]
   [essen.system.entity :as entity]
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

(defn event->context [scene {:event/keys [entity handler content] :as event}]
  (for [component (get-components scene event)]
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
     :context/event content}))
