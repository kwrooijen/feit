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

(defn derive-all-composites
  "Recursively apply `it/derive-composite` on all map keys."
  [m]
  (let [f (fn [[k v]]
            (when (coll? k)
              (try (it/derive-composite k)
                   (catch #?(:clj Throwable :cljs :default) _
                     ;; This means this key already derives from the parent
                     nil)))
            [k v])]
    (postwalk (fn [x] (if (map? x)
                        (into {} (map f x))
                        x))
              m)))

(defn derive-all-hierarchies [m]
  (postwalk
   (fn [x]
     (when-let [hierarchy (:keyword/hierarchy x)]
       (it/derive-hierarchy hierarchy))
     x)
   m))

(defn resolve-ref [ref]
  (->> ref
       (:key)
       (it/find-derived-value @state/system)))

(defn resolve-all [m]
  (postwalk #(if (ig/ref? %)
               (resolve-ref %)
               %)
            m))

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
