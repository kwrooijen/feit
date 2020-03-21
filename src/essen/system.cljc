(ns essen.system
  (:require
   [clojure.walk :refer [postwalk]]
   [com.rpl.specter :as specter :refer [MAP-VALS ALL LAST] :refer-macros [transform]]
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(def ^:private default-keys
  {:context/entity (ig/ref :context/entity)
   :context/scene (ig/ref :context/scene)
   :scene/opts (ig/ref :scene/opts)})

(defn- derive-composite-all
  "Recursively apply `it/derive-composite` on all map keys."
  [m]
  (let [f (fn [[k v]]
            (when (coll? k)
              (it/derive-composite k))
            [k v])]
    (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn- add-context [v]
  (cond-> v
    (map? v) (merge v default-keys)))

(defn- add-context-to-entities
  "Add a refence to context for all keys. This is necessary so that components
  know which entity / scene they belong to."
  [config]
  (->> (ig/find-derived config :essen/entity)
       (transform [ALL LAST MAP-VALS] add-context)
       (into {})
       (merge config)))

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
  (->> config
       (add-context-to-entities)
       (transform [MAP-VALS] add-context)
       (ig/prep)))
