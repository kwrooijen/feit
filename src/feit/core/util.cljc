(ns feit.core.util
  (:require
   [clojure.walk :refer [postwalk]]
   [integrant.core :as ig]
   [feit.core.state :as state]
   [integrant-tools.core :as it]))

(defn top-key [k]
  (if ^boolean (coll? k) (last k) k))

(defn bottom-key [k]
  (if ^boolean (coll? k) (first k) k))

(defn resolve-ref [{:keys [key]}]
  (it/find-derived-value @state/system key))

(defn resolve-all [m]
  (postwalk #(cond-> % (ig/ref? %) resolve-ref) m))

(defn map-kv [f m]
  (->> m
       (map (fn [[k v]] [k (f k v)]))
       (into {})))

(defn ->context
  ([scene-key]
   {:context/scene-key scene-key})
  ([scene-key entity-key]
   {:context/scene-key scene-key
    :context/entity-key entity-key})
  ([scene-key entity-key component-key]
   {:context/scene-key scene-key
    :context/entity-key entity-key
    :context/component-key component-key}))

(defn ->vec [v]
  (if (vector? v) v [v]))
