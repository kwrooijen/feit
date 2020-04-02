(ns essen.system.entity
  (:require
   [clojure.set]
   [meta-merge.core :refer [meta-merge]]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.system :as system]
   [essen.util :refer [vec->map top-key spy]]
   [integrant.core :as ig]))

(defn path-state
  [entity component]
  [:scene/entities entity
   :entity/components (:component/key component)
   :component/state])

(defn state [{:entity/keys [components]}]
  (transform [MAP-VALS] :component/state components))

(defn components->nested-routes [components]
  (for [{:component/keys [key handlers]} components
        k (keys handlers)]
    {k [key]}))

(defn- routes [{:entity/keys [components]}]
  (apply merge-with into (components->nested-routes components)))

(defn merge-extra-opts [component]
  (if-let [ref (:component/ref component)]
    (assoc ref :component/opts
           (meta-merge (:component/opts ref)
                       (dissoc component :component/ref)))
    component))

(defn process-components [components]
  (-> (mapv merge-extra-opts components)
      (vec->map :component/key)))

(defmethod system/init-key :essen/entity [k opts]
  (-> opts
      (update :entity/components process-components)
      (select-keys [:entity/components
                    :entity/dynamic])
      (assoc :entity/routes (routes opts)
             :entity/key (top-key k)
             :entity/opts (dissoc opts :entity/components)
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
