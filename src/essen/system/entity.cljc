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
  (or (get @persistent-entities k)
      (let [top-key (last k)]
        ;; This is to be able to subscribe to entity groups
        (it/derive-composite k)
        (-> opts
            (update :entity/components vec->map :component/key)
            (assoc :entity/routes (routes opts)
                   :entity/key top-key
                   :entity/persistent (:persistent (meta k)))
            (->> (merge (ig/init-key k opts)))))))

;; TODO Add a way to group entities (possibly through deriving / hierarchy?)
;; TODO Add a way to have some sort of "entity creator". You don't want to use
;; ig/init-key everytime you spawn a bullet.


;; TODO Maybe use this to generate ^:dynamic entites.
;; You define an entity in your config `[:essen/entity :entity/foo]`
;; But when creating an entity you'll get the key `:entity/foo+1` and `:entity/foo+2`
;; This way you can have multiple instances of the same entity.
;;
;; (derive :entity/foo+1 :entity/foo)
;; (init-entity config scene :entity/foo+1)
;; (Now :entity/foo+1 will init with :entity/foo config, but will have a unique name)
