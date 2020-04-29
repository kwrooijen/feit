(ns rooij.system.core
  (:require
   [rooij.config]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [meta-merge.core :refer [meta-merge]]
   [taoensso.timbre :as timbre]
   [rooij.methods :refer [assert-schema-key]]
   [rooij.state :as state]
   [rooij.util :refer [derive-composite-all vec->map]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [make-child]]
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

(defn init
  "Starts an rooij system (scene or entity). This is used internally by rooij
  and should not be called directly."
  [config key]
  (ig/build config [key] init-key assert-schema-key ig/resolve-key))

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

(def process-refs-keys
  (memoize
   (fn [ns]
     {:key (keyword ns "key")
      :ref (keyword ns "ref")
      :dynamic (keyword ns "dynamic")})))

(defn- merge-extra-opts [ks opts]
  (-> opts
      (meta-merge (get opts (:ref ks)))
      (dissoc (:ref ks))))

(defn process-refs [ref-opts ns]
  (when ref-opts
    (let [ks (process-refs-keys ns)]
      (->> ref-opts
           (sp/transform [MAP-VALS] (partial merge-extra-opts ks))
           (map (fn [[k v]] [k (assoc v (:key ks) k)]))
           (into {})))))

(defn get-halt-key [derived-k entity-opts]
  (if-let [f (get-method ig/halt-key! (#'ig/normalize-key derived-k))]
    (or (f derived-k entity-opts)
        (fn [_] nil))
    (fn [_] nil)))

(defn start []
  (timbre/info ::start)
  (rooij.config/reset-config! (prep @rooij.config/config))
  (-> @rooij.config/config
      (init [:rooij/system])
      (->> (reset! state/system))))
