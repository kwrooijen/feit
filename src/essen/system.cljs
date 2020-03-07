(ns essen.system
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(defmulti init-key
  (fn [key _]
    (#'ig/normalize-key key)))

(defmethod init-key :default [k opts]
  (ig/init-key k opts))

(defmethod it/init-fn :essen/init [_ k opts]
  (init-key k opts))
