(ns essen.system.entity
  (:require
   [essen.system :as es]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [essen.state :refer [persistent-entities]]
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

(defmethod es/init-key :essen/entity [k opts]
      (let [top-key (last k)]
        ;; This is to be able to subscribe to entity groups
        ;; TODO Maybe we want a scene hierarchy? That way we don't polute the
        ;; global keyword hierarchy
        (it/derive-composite k)
        (-> opts
            (update :entity/components vec->map :component/key)
            (assoc :entity/routes (routes opts)
                   :entity/key top-key)
            (->> (merge (ig/init-key k opts))))))

(defn start [config key]
  (let [ig-key (it/find-derived-key config key)
        dynamic?  (:dynamic (meta ig-key))
        v (it/find-derived-value config key)
        key (if dynamic? (conj ig-key (it.keyword/make-child key)) key)
        config (if dynamic? (assoc config key v) config)
        config (assoc config [:it/const :entity/opts] {:entity/key key})
        system (it/init config [:essen/init] [key])]
    (with-meta
      (it/find-derived-value system key)
      {:system system})))

(defn stop! [entity]
  (ig/halt! (:system (meta entity))))

;; TODO Add a way to have some sort of "entity creator". You don't want to use
;; ig/init-key everytime you spawn a bullet.
