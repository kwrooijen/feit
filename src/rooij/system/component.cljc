(ns rooij.system.component
  (:require
   [taoensso.timbre :as timbre]
   [integrant.core :as ig]
   [rooij.util :refer [vec->map top-key]]
   [rooij.state :as state]
   [rooij.system.core :as system]))

(def init-dissocs
  [:component/init
   :component/alpha-key
   :component/key
   :component/state
   :component/handlers
   :component/tickers
   :component/opts])

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

(defn get-persistent-state [{:context/keys [entity-key] :component/keys [key alpha-key] :as opts}]
  (when-let [state (state/get-component entity-key key)]
    (persistent-resume alpha-key opts state)))

(defn save-persistent-component!
  [{:component/keys [key opts state] :context/keys [entity-key] :as component}]
  (when (:component/persistent opts)
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
             :component/alpha-key (top-key k)
             :component/init (system/get-init-key k)
             :component/state nil
             :component/halt! (system/get-halt-key k opts)
             :component/opts (dissoc opts
                                     :component/handlers
                                     :component/tickers
                                     :component/reactors))))

(defn init
  [{:component/keys [key init] :as component}]
  (timbre/debug ::start component)
  (-> component
      (assoc :component/state
             (or (get-persistent-state component)
                 (init key (reduce dissoc component init-dissocs))))
      (save-persistent-component!)))

(defn prep [component context]
  (-> component
      (merge (:component/opts component) context)
      (update :component/tickers system/process-refs :ticker)
      (update :component/handlers system/process-refs :handler)
      (update :component/reactors system/process-refs :reactor)))
