(ns essen.system.component
  (:require
   [integrant.core :as ig]
   [essen.util :refer [vec->map top-key spy]]
   [essen.state :refer [persistent-components]]
   [essen.system.core :as system]))

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
  (when-let [state (get @persistent-components [entity-key key])]
    (persistent-resume alpha-key opts state)))

(defn save-persistent-component!
  [{:component/keys [key opts state] :context/keys [entity-key] :as component}]
  (when (:component/persistent opts)
    (swap! persistent-components assoc [entity-key key] state))
  component)

(defmethod system/init-key :essen/component [k opts]
  (-> opts
      (select-keys [:component/tickers
                    :component/handlers
                    :component/reactors
                    :component/persistent])
      (assoc :component/key (top-key k)
             :component/alpha-key (top-key k)
             :component/init (system/method k)
             :component/state nil
             :component/halt! (system/get-halt-key k opts)
             :component/opts (dissoc opts
                                     :component/handlers
                                     :component/tickers
                                     :component/reactors))
      (update :component/tickers vec->map :ticker/key)
      (update :component/handlers vec->map :handler/key)
      (update :component/reactors vec->map :reactor/key)))

(defn start
  [{:context/keys [scene-key entity-key] :component/keys [key state init] :as component}]
  (try
    (-> component
        (assoc :component/state
               (or (get-persistent-state component)
                   (init key (:component/opts component))))
        (save-persistent-component!))
    (catch #?(:clj Throwable :cljs :default) t
      (println "[ERROR] Failed to start component.\n"
               "Scene:" scene-key "\n"
               "Entity:" entity-key "\n"
               "Component:" key "\n"
               "Reason:" (ex-data t)))))
