(ns essen.system
  (:require
   [integrant.core :as ig]))

(defmulti init-key
  (fn [key _]
    (#'ig/normalize-key key)))

(defmethod init-key :default [k opts]
  (ig/init-key k opts))
