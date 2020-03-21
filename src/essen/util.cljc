(ns essen.util)

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
