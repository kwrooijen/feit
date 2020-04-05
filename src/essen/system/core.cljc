(ns essen.system.core
  (:require
   [cljs.pprint]
   [essen.methods :refer [assert-schema-key]]
   [essen.state :as state]
   [essen.util :refer [derive-composite-all]]
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(it/derive-hierarchy
 {:essen/scene [:essen/system]
  :essen/entity [:essen/system]
  :essen/component [:essen/system]
  :essen/handler [:essen/system]
  :essen/keyboard [:essen/system]
  :essen/reactor [:essen/system]
  :essen/ticker [:essen/system]})

(defmulti init-key
  "The init-key for essen system components. This is used internally by essen
  and should not be called directly."
  (fn [key _]
    (#'ig/normalize-key key)))

(defmethod init-key :default [k opts]
  (ig/init-key k opts))

(defn init
  "Starts an essen system (scene or entity). This is used internally by essen
  and should not be called directly."
  [config key]
  (ig/build config [key] init-key assert-schema-key ig/resolve-key))

(defn prep
  "Prepares the config system with a composite derive on all keys. This is used
  internally by essen and should not be called directly."
  [config]
  (derive-composite-all config)
  config)

(defn get-init-key [derived-k]
  (if-let [f (get-method ig/init-key (#'ig/normalize-key derived-k))]
    f
    (fn [_ opts] opts)))

(defn get-halt-key [derived-k entity-opts]
  (if-let [f (get-method ig/halt-key! (#'ig/normalize-key derived-k))]
    (or (f derived-k entity-opts)
        (fn [_] nil))
    (fn [_] nil)))

(defn start [config]
  (reset! state/config (prep config))
  (-> @state/config
      (init [:essen/system])
      (->> (reset! state/system))))
