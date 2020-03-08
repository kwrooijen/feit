(ns essen.system.entity
  (:require
   [essen.system :as es]
   [integrant-tools.core :as it]
   [essen.state :refer [game persistent-entities]]
   [essen.util :refer [vec->map]]
   [integrant.core :as ig]))

(defn path-state
  [entity component]
  [:scene/entities entity
   :entity/components (:component/key component)
   :component/state])

(defn- routes [{:entity/keys [components]}]
  (apply merge
         (for [{:component/keys [key handlers]} components
               k (keys handlers)]
           {k key})))

(defmethod ig/init-key :essen/entity [_ entity]
  entity)

(defmethod ig/init-key :essen/scene [_ entity]
  entity)

(defmethod es/init-key :essen/entity [k opts]
  (or (get @persistent-entities (last k))
      (let [top-key (last k)]
        ;; This is to be able to subscribe to entity groups
        (it/derive-composite k)
        (-> opts
            (update :entity/components vec->map :component/key)
            (assoc :entity/routes (routes opts)
                   :entity/key top-key
                   :entity/persistent (:persistent (meta k)))
            (->> (merge (ig/init-key k opts)))))))

;; TODO Add a way to have some sort of "entity creator". You don't want to use
;; ig/init-key everytime you spawn a bullet.
