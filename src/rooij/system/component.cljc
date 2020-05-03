(ns rooij.system.component
  (:require
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [rooij.system.ticker :as ticker]
   [rooij.system.handler :as handler]
   [rooij.system.reactor :as reactor]
   [rooij.system.middleware :as middleware]
   [rooij.state :as state]
   [rooij.system.core :as system]
   [taoensso.timbre :as timbre]))

(def init-dissocs
  [:component/init
   :component/halt!
   :component/key
   :component/handlers
   :component/tickers
   :component/reactors
   :component/middleware
   :component/state])

(defn path
  ([entity]
   [:scene/entities entity
    :entity/components])
  ([entity component]
   [:scene/entities entity
    :entity/components component]))

(defn save-persistent-component!
  [{:component/keys [key state persistent auto-persistent]
    :context/keys [entity-key] :as component}]
  (when (or persistent auto-persistent)
    (state/save-component! state entity-key key))
  component)

(defmethod system/init-key :rooij/component [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :component/init (system/get-init-key k)
         :component/state nil
         :component/halt! (system/get-halt-key k opts)))

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
      (merge context)
      (update :component/tickers  #(sp/transform [MAP-VALS] ticker/init %))
      (update :component/handlers #(sp/transform [MAP-VALS] handler/init %))
      (update :component/reactors #(sp/transform [MAP-VALS] reactor/init %))
      (update :component/middlewares #(sp/transform [MAP-VALS] middleware/init %))))
