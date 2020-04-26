(ns rooij.system.entity
  (:require
   [taoensso.timbre :as timbre]
   [com.rpl.specter :as sp :refer [MAP-VALS MAP-KEYS ALL]]
   [rooij.system.core :as system]
   [rooij.util :refer [top-key]]
   [integrant-tools.keyword :refer [make-child]]
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

(defn- get-top-key [k opts]
  (if (:entity/dynamic opts)
    (make-child (top-key k))
    (top-key k)))

(defmethod system/init-key :rooij/entity [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (update :entity/components system/process-refs :component)
      (select-keys [:entity/components
                    :entity/dynamic
                    :entity/subs])
      (assoc :entity/key (get-top-key k opts)
             :entity/opts (dissoc opts :entity/components)
             :entity/init (system/get-init-key k)
             :entity/halt! (system/get-halt-key k opts))))

(defn entity-component-state [{:entity/keys [components]}]
  (sp/transform [MAP-VALS] :component/state components))

(defn insert-new-component-state [entity [component-key component-state]]
  (assoc-in entity
            [:entity/components component-key :component/state]
            component-state))

;; TODO Create prep function (like component)
(defn init [{entity-key :entity/key scene-key :context/scene-key :as entity}]
  (timbre/debug ::start entity)
  (-> (entity-component-state entity)
      (assoc :context/scene-key scene-key)
      (->> ((:entity/init entity) entity-key))
      (dissoc :context/scene-key)
      (->> (reduce insert-new-component-state entity))
      (add-routes)))

(defn halt! [{:entity/keys [components] :as entity}]
  ;; TODO remove dynamic entity
  (doseq [[_ component] components]
    ((:component/halt! component) component))
  ((:entity/halt! entity) entity))

(defn suspend! [entity]
  (doseq [[component-key component] (:entity/components entity)]
    (ig/suspend-key! component-key component))
  (ig/suspend-key! (:entity/key entity) entity))
