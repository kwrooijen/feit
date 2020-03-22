(ns essen.util
  (:require
   [clojure.string :as string]
   [meta-merge.core :refer [meta-merge]]))

(defn vec->map [v k]
  (reduce #(assoc %1 (get %2 k) %2) {} (flatten v)))

(defn spy
  ([v] (spy v identity))
  ([v f]
   (println "SPY: " (f v))
   v))

(defn key-ns? [n]
  (comp #{n}
        keyword
        namespace
        first))

(defn keep-ns [m n]
  (->> m
       (filter (key-ns? n))
       (into {})))

(defn top-key [k]
  (if (coll? k) (last k) k))

(defn keyword->path [k]
  (->> (conj (string/split (namespace k) #"\.") (name k))
       (map keyword)))

(defn ns-kv->map [[k v]]
  (assoc-in {} (keyword->path k) v))

(defn top-ns-key [k]
  (first (keyword->path k)))

(defn get-top-key [{:keys [top-keys]} m]
  (if top-keys
    (select-keys m top-keys) m))

(defn ns-map->nested-map
  ([m] (ns-map->nested-map {} m))
  ([opts m]
   (->> m
        (map ns-kv->map)
        (apply meta-merge)
        (get-top-key opts))))
