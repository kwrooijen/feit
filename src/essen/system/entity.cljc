(ns essen.system.entity
  (:require
   [clojure.set]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.system :as system]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [essen.util :refer [vec->map top-key spy]]
   [meta-merge.core :refer [meta-merge]]
   [essen.state :as state]
   [integrant.core :as ig]))

(defn path-state
  [entity component]
  [:scene/entities entity
   :entity/components (:component/key component)
   :component/state])

(defn state [{:entity/keys [components]}]
  (transform [MAP-VALS] :component/state components))

(defn components->nested-handlers [components]
  (for [{:component/keys [key handlers]} components
        k (keys handlers)]
    {k key}))

(defn- handlers [{:entity/keys [components]}]
  (apply merge (components->nested-handlers components)))

(defmethod system/init-key :essen/entity [k opts]
  (-> opts
      ;; TODO Check if any components are direct children of :essen/component
      ;; If they are, throw an error. You're not allowed to use root components
      (update :entity/components vec->map :component/key)
      ;; TODO Rename handlers back to routes
      (select-keys [:entity/components])
      (assoc :entity/handlers (handlers opts)
             :entity/key (top-key k)
             :entity/init (system/get-init-key k opts)
             :entity/halt! (system/get-halt-key k opts))))

(defn halt! [{:entity/keys [components] :as entity}]
  ;; TODO remove dynamic entity
  (doseq [[_ component] components]
    ((:component/halt! component) component))
  ((:entity/halt! entity) entity))

(defn suspend! [entity]
  (doseq [[component-key component] (:entity/components entity)]
    (ig/suspend-key! component-key component))
  (ig/suspend-key! (:entity/key entity) entity))
