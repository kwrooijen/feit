(ns feit.system.component
  (:require [meta-merge.core :refer [meta-merge]]
            [feit.core.state :as state]
            [feit.system.core :as system]
            [feit.system.handler :as handler :refer [preprocess-handlers]]
            [feit.system.middleware :as middleware :refer [preprocess-middlewares]]
            [feit.system.reactor :as reactor :refer [preprocess-reactors]]
            [feit.system.ticker :as ticker :refer [preprocess-tickers]]
            [feit.system.keyboard :as keyboard :refer [preprocess-keyboards]]
            [feit.core.util :refer [->context map-kv]]
            [taoensso.timbre :as timbre]))

(def init-dissocs
  [:component/init
   :component/halt!
   :component/key
   :component/handlers
   :component/tickers
   :component/reactors
   :component/middlewares
   :component/state])

(defn path
  ([entity]
   [:scene/entities entity
    :entity/components])
  ([entity component]
   [:scene/entities entity
    :entity/components component]))

(defn- save-persistent-component!
  [{:component/keys [key state persistent auto-persistent]
    :context/keys [entity-key] :as component}]
  (when (or persistent auto-persistent)
    (state/save-component! state entity-key key))
  component)

(defmethod system/init-key :feit/component [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :component/init (system/get-init-key k)
         :component/state nil
         :component/halt! (system/get-halt-key k opts)))

(defn check-entity-hierarchy [entity-key]
  (when-not (isa? entity-key :feit/entity)
    (throw (ex-info (str entity-key " is not related to :feit/entity.\n\n"
                         "This usually means that " entity-key " is derived from "
                         "another key, which should be an entity.\n\n"
                         "Ancestors: " (ancestors entity-key)) ::invalid-entity))))

(defn get-init-state
  [{:component/keys [auto-persistent init key] :context/keys [entity-key] :as component}]
  (check-entity-hierarchy entity-key)
  (let [persistent-state (state/get-component entity-key key)
        component (assoc component :context/state persistent-state)]
    (if (and auto-persistent persistent-state)
      persistent-state
      (-> key
          (init (reduce dissoc component init-dissocs))
          (dissoc :context/scene-key
                  :context/entity-key
                  :context/component-key
                  :context/state)))))

(defn process-refs-component [{:context/keys [scene-key entity-key component-key] :as opts}]
  (-> opts
      (update :component/handlers (partial preprocess-handlers scene-key entity-key component-key))
      (update :component/tickers (partial preprocess-tickers scene-key entity-key component-key))
      (update :component/reactors (partial preprocess-reactors scene-key entity-key component-key))
      (update :component/middlewares (partial preprocess-middlewares scene-key entity-key component-key))
      (update :component/keyboards (partial preprocess-keyboards scene-key entity-key component-key))))

(defn preprocess-component [context component-key component-opts]
  (-> component-opts
      (->> (meta-merge (:component/ref component-opts)))
      (dissoc :component/ref)
      (system/merge-context context)
      (assoc :component/key component-key)
      (as-> $ (assoc $ :component/state (get-init-state $)))
      (process-refs-component)
      (save-persistent-component!)))

(defn preprocess-components [scene-key entity-key components]
  (map-kv #(preprocess-component (->context scene-key entity-key %1) %1 %2) components))
