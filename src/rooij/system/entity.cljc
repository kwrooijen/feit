(ns rooij.system.entity
  (:require
   [taoensso.timbre :as timbre]
   [meta-merge.core :refer [meta-merge]]
   [com.rpl.specter :as sp :refer [MAP-VALS MAP-KEYS ALL]]
   [rooij.system.core :as system]
   [rooij.util :refer [vec->map top-key]]
   [integrant.core :as ig]))

(defn path-state
  [entity component]
  [:scene/entities entity
   :entity/components (:component/key component)
   :component/state])

(defn state [{:entity/keys [components]}]
  (sp/transform [MAP-VALS] :component/state components))

(defn has-handler? [handler-key component]
  ((set (sp/select [:component/handlers MAP-VALS :handler/key] component))
   handler-key))

(defn filter-handler-components [handler-key components]
  (mapv :component/key
        (filter #(has-handler? handler-key %) components)))

(defn components->nested-routes [handler-keys components]
  (for [handler-key handler-keys]
    {handler-key (filter-handler-components handler-key components)}))

(defn routes [entity]
  (let [components (sp/select [:entity/components MAP-VALS] entity)
        handler-keys (sp/select [ALL :component/handlers MAP-KEYS] components)]
    (->> components
         (components->nested-routes handler-keys)
         (apply merge))))

(defn add-routes [entity] []
  (assoc entity :entity/routes (routes entity)))

(defn set-component-key [ref {:component/keys [key]}]
  (if key
    (assoc ref :component/key key)
    ref))

(defn set-component-opts [ref component]
  (update ref :component/opts meta-merge (dissoc component :component/ref)))

(defn merge-extra-opts [component]
  (if-let [ref (:component/ref component)]
    (-> ref
        (set-component-key  component)
        (set-component-opts  component))
    component))

(defn process-components [components]
  (-> (mapv merge-extra-opts components)
      (vec->map :component/key)))

(defmethod system/init-key :rooij/entity [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (update :entity/components process-components)
      (select-keys [:entity/components
                    :entity/dynamic])
      (assoc :entity/key (top-key k)
             :entity/opts (dissoc opts :entity/components)
             :entity/init (system/get-init-key k)
             :entity/halt! (system/get-halt-key k opts))))

;; TODO Create prep function (like component)
(defn init [{entity-key :entity/key opts :entity/opts :as entity}]
  (timbre/debug ::start entity)
  ((:entity/init entity) entity-key opts)
  (add-routes entity))

(defn halt! [{:entity/keys [components] :as entity}]
  ;; TODO remove dynamic entity
  (doseq [[_ component] components]
    ((:component/halt! component) component))
  ((:entity/halt! entity) entity))

(defn suspend! [entity]
  (doseq [[component-key component] (:entity/components entity)]
    (ig/suspend-key! component-key component))
  (ig/suspend-key! (:entity/key entity) entity))
