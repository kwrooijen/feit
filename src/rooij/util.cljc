(ns rooij.util
  (:require
   [clojure.walk :refer [postwalk]]
   [integrant-tools.core :as it]
   [clojure.string :as string]
   [meta-merge.core :refer [meta-merge]]))

(defn vec->map [v k]
  (reduce #(assoc %1 (get %2 k) %2) {} (flatten v)))

(defn top-key [k]
  (if ^boolean (coll? k) (last k) k))

(defn derive-composite-all
  "Recursively apply `it/derive-composite` on all map keys."
  [m]
  (let [f (fn [[k v]]
            (when (coll? k)
              (it/derive-composite k))
            [k v])]
    (doall
     (postwalk (fn [x] (if ^boolean (map? x) (into {} (map f x)) x)) m))))
