(ns essen.system
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]))

(defmulti init-key
  (fn [key _]
    (#'ig/normalize-key key)))

(defmethod init-key :default [k opts]
  (ig/init-key k opts))

(defn init [config key]
  (ig/build config [key] init-key ig/assert-pre-init-spec ig/resolve-key))
