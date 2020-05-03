(ns rooij.system.entity
  (:require
   [taoensso.timbre :as timbre]
   [com.rpl.specter :as sp :refer [MAP-VALS MAP-KEYS ALL]]
   [rooij.system.core :as system]
   [rooij.util :refer [top-key]]
   [integrant.core :as ig]))

(defn- has-handler? [handler-key component]
  ((set (sp/select [:component/handlers MAP-VALS :handler/key] component))
   handler-key))

(defn- filter-handler-components [handler-key components]
  (mapv :component/key
        (filter #(has-handler? handler-key %) components)))

(defn- components->nested-routes [handler-keys components]
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

(defmethod system/init-key :rooij/entity [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (assoc :entity/key (top-key k)
             :entity/init (system/get-init-key k)
             :entity/halt! (system/get-halt-key k opts))))

(defn- entity-component-state [{:entity/keys [components]}]
  (sp/transform [MAP-VALS] :component/state components))

;; TODO Create prep function (like component)
(defn init [{entity-key :entity/key
             scene-key :context/scene-key
             :as entity}]
  (timbre/debug ::start entity)
  (-> entity
      (assoc :context/scene-key scene-key
             :context/entity-key entity-key
             :entity/state (entity-component-state entity))
      (->> ((:entity/init entity) entity-key))
      (add-routes)))

(defn halt! [{:entity/keys [components] :as entity}]
  ;; TODO remove dynamic entity
  ;; OR Create local hierarchy for scenes
  (doseq [[_ component] components]
    ((:component/halt! component) component))
  ((:entity/halt! entity) entity))

(defn suspend! [entity]
  (doseq [[component-key component] (:entity/components entity)]
    (ig/suspend-key! component-key component))
  (ig/suspend-key! (:entity/key entity) entity))
