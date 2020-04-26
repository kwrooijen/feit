(ns rooij.system.core
  (:require
   [rooij.config]
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
      :opts (keyword ns "opts")
      :ref (keyword ns "ref")
      :dynamic (keyword ns "dynamic")})))

(defn set-ref-dynamic-key [ref ks opts]
  (if (or (get ref (:dynamic ks))
          (get opts (:dynamic ks)))
    (update ref (:key ks) make-child)
    ref))

(defn set-component-opts [ref ks opts]
  (update ref (:opts ks) meta-merge (dissoc opts (:ref ks))))

(defn merge-extra-opts [ks opts]
  (if-let [ref (get opts (:ref ks))]
    (-> ref
        (set-ref-dynamic-key ks opts)
        (set-component-opts ks opts))
    opts))

(defn process-refs [ref-opts ns]
  (let [ks (process-refs-keys ns)]
    (-> (mapv (partial merge-extra-opts ks) (remove nil? ref-opts))
        (vec->map (:key ks)))))

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
