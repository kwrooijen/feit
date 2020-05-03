(ns rooij.system.component
  (:require [meta-merge.core :refer [meta-merge]]
            [rooij.state :as state]
            [rooij.system.core :as system]
            [rooij.system.handler :as handler :refer [preprocess-handlers]]
            [rooij.system.middleware :as middleware :refer [preprocess-middlewares]]
            [rooij.system.reactor :as reactor :refer [preprocess-reactors]]
            [rooij.system.ticker :as ticker :refer [preprocess-tickers]]
            [rooij.util :refer [->context map-kv]]
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

(defn- save-persistent-component!
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
                  :context/component-key
                  :context/state)))))

(defn preprocess-component [context component-key component-opts]
  (-> component-opts
      (->> (meta-merge (:component/ref component-opts)))
      (dissoc :component/ref)
      (merge context)
      (assoc :component/key component-key)
      (as-> $ (assoc $ :component/state (get-init-state $)))
      (save-persistent-component!)))


(defn preprocess-components [scene-key entity-key components]
  (map-kv #(preprocess-component (->context scene-key entity-key %1) %1 %2) components))

(defn process-refs-component [{:context/keys [scene-key entity-key component-key] :as opts}]
  (-> opts
      (update :component/handlers (partial preprocess-handlers scene-key entity-key component-key))
      (update :component/tickers (partial preprocess-tickers scene-key entity-key component-key))
      (update :component/reactors (partial preprocess-reactors scene-key entity-key component-key))
      (update :component/middlewares (partial preprocess-middlewares scene-key entity-key component-key))))
