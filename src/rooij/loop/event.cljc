(ns rooij.loop.event
  (:require
   [rooij.system.component :as component]
   [integrant-tools.keyword :refer [ancestor?]]))

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
  (let [component-key (:component/key component)
        entity-state (-> scene :scene/entities entity :entity/state)]
    {:context/scene-key (:scene/key scene)
     :context/entity-key entity
     :context/component-key component-key
     :context/scene scene
     :context/entity entity-state
     :context/component component
     :context/state (get entity-state component-key)
     :context/old-state (get entity-state component-key)
     :context/handler (get-in component [:component/handlers handler])
     :context/handler-key handler
     :context/event content}))

(defn event->contexts [scene {:event/keys [excludes] :as event}]
  (reduce
   (fn [acc component]
     (if ^boolean (excludable? component excludes)
       acc
       (conj acc (event->context scene event component))))
   []
   (get-components scene event)))
