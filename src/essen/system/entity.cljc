(ns essen.system.entity
  (:require
   [clojure.set]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.system :as es]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [essen.util :refer [vec->map]]
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]))

(defn path-state
  [entity component]
  [:scene/entities entity
   :entity/components (:component/key component)
   :component/state])

(defn state [{:entity/keys [components]}]
  (transform [MAP-VALS] :component/state components))

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
               :entity/key top-key))))

(defn start [config key]
  (let [v (it/find-derived-value config key)
        k (it/find-derived-key config key)
        v ((ig/init-key k v) v)
        dynamic? (:entity/dynamic v)
        key (if dynamic? (it.keyword/make-child key) key)

        config (meta-merge config (dissoc v
                                          :entity/dynamic
                                          :entity/components
                                          :entity/subs
                                          :scene/opts
                                          :entity/opts))
        config (assoc config [:it/const :entity/opts] {:entity/key key})

        config (if dynamic? (clojure.set/rename-keys config {k key})
                   config)
        system (es/init config [key])]
    (with-meta
      (it/find-derived-value system key)
      {:system system})))

(defn stop! [entity]
  (ig/halt! (:system (meta entity))))

(defn suspend! [entity]
  (try (ig/suspend! (:system (meta entity)))
       (catch #?(:clj Throwable :cljs :default) t
         (println "Failed to halt entity" (:entity/key entity)
                  "because of key" (:key (ex-data t)))
         (println "Reason: " (:reason (ex-data t))))))

;; TODO Add a way to have some sort of "entity creator". You don't want to use
;; ig/init-key everytime you spawn a bullet.
