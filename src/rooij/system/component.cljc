(ns rooij.system.component
  (:require
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [rooij.system.ticker :as ticker]
   [rooij.system.handler :as handler]
   [rooij.system.reactor :as reactor]
   [rooij.system.middleware :as middleware]
   [rooij.state :as state]
   [rooij.system.core :as system]
   [rooij.util :refer [top-key]]
   [taoensso.timbre :as timbre]))

(def init-dissocs
  [:component/init
   :component/halt!
   :component/key
   :component/handlers
   :component/tickers
   :component/reactors
   :component/middleware
   :component/state
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
                    :component/middleware
                    :component/persistent
                    :component/original-key])
      (assoc :component/key (top-key k)
             :component/init (system/get-init-key k)
             :component/state nil
             :component/halt! (system/get-halt-key k opts)
             :component/opts (dissoc opts
                                     :component/handlers
                                     :component/tickers
                                     :component/original-key
                                     :component/reactors
                                     :component/middleware))))

(defn get-init-state
  [{:component/keys [auto-persistent init key] :context/keys [entity-key] :as component}]
  (let [persistent-state (state/get-component entity-key key)
        component (assoc component :context/state persistent-state)]
    (if (and auto-persistent persistent-state)
      persistent-state
      (-> key
          (init (reduce dissoc component init-dissocs))
          (dissoc :context/scene-key
                  :context/entity-key
                  :context/state)))))

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
      (update :component/reactors system/process-refs :reactor)
      (update :component/middlewares system/process-refs :middleware)
      (update :component/tickers  #(sp/transform [MAP-VALS] ticker/init %))
      (update :component/handlers #(sp/transform [MAP-VALS] handler/init %))
      (update :component/reactors #(sp/transform [MAP-VALS] reactor/init %))
      (update :component/middlewares #(sp/transform [MAP-VALS] middleware/init %))))
