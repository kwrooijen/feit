(ns essen.system.component
  (:require
   [integrant.core :as ig]
   [essen.util :refer [vec->map top-key]]
   [essen.state :refer [persistent-components]]
   [essen.system :as es]))

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

(defn get-persistent-state [k {:context/keys [entity] :as opts}]
  (when-let [state (get @persistent-components [entity (top-key k)])]
    (persistent-resume k opts (:component/state state))))

(defn save-persistent-component!
  [{:component/keys [key persistent] :as component} entity-key]
  (when persistent
    (swap! persistent-components assoc [entity-key key] component))
  component)

(defmethod es/init-key :essen/component [k opts]
  (-> opts
      (select-keys [:component/tickers
                    :component/handlers
                    :component/reactors
                    :component/persistent])
      (assoc :component/key (top-key k)
             :component/state (or (get-persistent-state k opts)
                                  (ig/init-key k opts)))
      (update :component/tickers vec->map :ticker/key)
      (update :component/handlers vec->map :handler/key)
      (update :component/reactors vec->map :reactor/key)
      (save-persistent-component! (:context/entity opts))))
