(ns essen.system.component
  (:require
   [integrant.core :as ig]
   [essen.util :refer [vec->map top-key]]
   [essen.state :refer [persistent-components]]
   [essen.system :as system]))

(defmulti persistent-resume
  (fn [key _opts _state]
    (#'ig/normalize-key key)))

(defmethod persistent-resume :default [_key _opts state] state)

(defn path
  ([entity]
   [:scene/entities entity
    :entity/components])
  ([entity component]
   [:scene/entities entity
    :entity/components component]))

(defn get-persistent-state [{:context/keys [entity] :component/keys [key] :as opts}]
  (when-let [state (get @persistent-components [entity key])]
    (persistent-resume key opts (:component/state state))))

(defn save-persistent-component!
  [{:component/keys [key persistent] :as component} entity-key]
  (when persistent
    (swap! persistent-components assoc [entity-key key] component))
  component)

(defmethod system/init-key :essen/component [k opts]
  (-> opts
      (select-keys [:component/tickers
                    :component/handlers
                    :component/reactors
                    :component/persistent])
      (assoc :component/key (top-key k)
             :component/state (system/get-init-key k opts)
             :component/halt! (system/get-halt-key k opts))
      (update :component/tickers vec->map :ticker/key)
      (update :component/handlers vec->map :handler/key)
      (update :component/reactors vec->map :reactor/key)))

(defn start
  [{:context/keys [scene entity] :component/keys [key state] :as component}]
  (try
    (-> component
        (assoc :component/state
               (or (get-persistent-state component)
                   (state component)))
        (save-persistent-component! entity))
    (catch #?(:clj Throwable :cljs :default) t
      (println "[ERROR] Failed to start component.\n"
               "Scene:" scene "\n"
               "Entity:" entity "\n"
               "Component:" key "\n"
               "Reason:" (ex-data t)))))
