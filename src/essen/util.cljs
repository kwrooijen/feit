(ns essen.util)

(defn vec->map [v k]
  (reduce #(assoc %1 (get %2 k) %2) {} v))
