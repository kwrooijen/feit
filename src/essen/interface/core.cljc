(ns essen.interface.core
  (:require [malli.core :as m]))

(defonce registry
  (atom m/default-registry))

(defn define-schema! [k v]
  (swap! registry assoc k (m/schema v {:registry @registry})))
