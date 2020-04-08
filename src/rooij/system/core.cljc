(ns rooij.system.core
  (:require
   [rooij.interface.graphics-2d.core :as interface.graphics-2d]
   [meta-merge.core :refer [meta-merge]]
   [taoensso.timbre :as timbre]
   [rooij.methods :refer [assert-schema-key]]
   [rooij.state :as state]
   [rooij.util :refer [derive-composite-all]]
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(it/derive-hierarchy
 {:rooij/scene [:rooij/system]
  :rooij/entity [:rooij/system]
  :rooij/component [:rooij/system]
  :rooij/handler [:rooij/system]
  :rooij/keyboard [:rooij/system]
  :rooij/reactor [:rooij/system]
  :rooij/ticker [:rooij/system]})

(defmulti init-key
  "The init-key for rooij system components. This is used internally by rooij
  and should not be called directly."
  (fn [key _]
    (#'ig/normalize-key key)))

(defmethod init-key :default [k opts]
  (ig/init-key k opts))

(defn merge-configs [config]
  (meta-merge interface.graphics-2d/config
              config))

(defn init
  "Starts an rooij system (scene or entity). This is used internally by rooij
  and should not be called directly."
  [config key]
  (-> config
      merge-configs
      (ig/build [key] init-key assert-schema-key ig/resolve-key)))

(defn prep
  "Prepares the config system with a composite derive on all keys. This is used
  internally by rooij and should not be called directly."
  [config]
  (derive-composite-all config)
  (ig/prep config))

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
  (timbre/info ::start)
  (reset! state/config (prep config))
  (-> @state/config
      (init [:rooij/system])
      (->> (reset! state/system))))
