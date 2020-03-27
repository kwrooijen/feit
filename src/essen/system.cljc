(ns essen.system
  (:require
   [clojure.walk :refer [postwalk]]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.state :as state]
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

(defn- derive-composite-all
  "Recursively apply `it/derive-composite` on all map keys."
  [m]
  (let [f (fn [[k v]]
            (when (coll? k)
              (it/derive-composite k))
            [k v])]
    (doall
     (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m))))

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
  (ig/build config [key] init-key ig/assert-pre-init-spec ig/resolve-key))

(defn prep
  "Prepares the config system by adding context references to all keys. Also
  does a composite derive on all keys. This is used internally by essen and
  should not be called directly."
  [config]
  (derive-composite-all config)
  (ig/prep config))

(defn get-init-key [derived-k entity-opts]
  (if-let [f (get-method ig/init-key (ig/normalize-key derived-k))]
    (f derived-k entity-opts)
    (fn [_ _] nil)))

(defn get-halt-key [derived-k entity-opts]
  (if-let [f (get-method ig/halt-key! (ig/normalize-key derived-k))]
    (or (f derived-k entity-opts)
        (fn [_] nil))
    (fn [_] nil)))

(defn start [config]
  (reset! state/config (prep config))
  (-> @state/config
      (init [:essen/scene])
      (->> (reset! state/system))))
