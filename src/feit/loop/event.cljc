(ns feit.loop.event
  (:require
   [feit.system.component :as component]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [ancestor?]]))

(defn- get-component-key [scene {:event/keys [entity handler]}]
  (ig/find-derived
   (get-in scene [:scene/entities entity :entity/routes])
   handler))

(defn ^boolean excludable? [{:component/keys [key]} excludes]
  (some (fn [exclude]
          (or (identical? key exclude)
              (ancestor? exclude key)))
        excludes))

(defn event->context [scene {:event/keys [entity content]} component handler-key]
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
     :context/handler (get-in component [:component/handlers handler-key])
     :context/handler-key handler-key
     :context/event content}))

(defn event->contexts [scene {:event/keys [excludes entity] :as event}]
  (let [contexts (volatile! [])]
      (doseq [[handler-key component-keys] (get-component-key scene event)
              component-key component-keys]
        (let [component (get-in scene (component/path entity component-key))]
          (when-not (excludable? component excludes)
            (vswap! contexts conj (event->context scene event component handler-key)))))
    @contexts))
