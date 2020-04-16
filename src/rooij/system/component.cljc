(ns rooij.system.component
  (:require
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]
   [rooij.util :refer [vec->map top-key]]
   [rooij.state :as state]
   [rooij.system.core :as system]))

(def init-dissocs
  [:component/init
   :component/key
   :component/handlers
   :component/tickers
   :component/opts])

(defn path
  ([entity]
   [:scene/entities entity
    :entity/components])
  ([entity component]
   [:scene/entities entity
    :entity/components component]))

(defn save-persistent-component!
  [{:component/keys [key opts state] :context/keys [entity-key] :as component}]
  (when (or (:component/persistent opts)
            (:component/auto-persistent opts))
    (state/save-component! state entity-key key))
  component)

(defmethod system/init-key :rooij/component [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (select-keys [:component/tickers
                    :component/handlers
                    :component/reactors
                    :component/persistent])
      (assoc :component/key (top-key k)
             :component/init (system/get-init-key k)
             :component/state nil
             :component/halt! (system/get-halt-key k opts)
             :component/opts (dissoc opts
                                     :component/handlers
                                     :component/tickers
                                     :component/reactors))))

(defn get-init-state
  [{:component/keys [auto-persistent init key] :context/keys [entity-key] :as component}]
  (let [persistent-state (state/get-component entity-key key)
        component (assoc component :context/state persistent-state)]
    (if (and auto-persistent persistent-state)
      persistent-state
      (init key (reduce dissoc component init-dissocs)))))

(defn init
  [{:component/keys [key] :as component}]
  (timbre/debug [::start key] component)
  (-> component
      (assoc :component/state (get-init-state component))
      (save-persistent-component!)))

(defn prep [component context]
  (-> component
      (merge (:component/opts component) context)
      (update :component/tickers system/process-refs :ticker)
      (update :component/handlers system/process-refs :handler)
      (update :component/reactors system/process-refs :reactor)))
