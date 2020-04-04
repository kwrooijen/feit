(ns essen.methods
  (:require
   [integrant.core :as ig]))

(defmulti doc-key
  "Markdown Document string"
  (fn [key]
    (#'ig/normalize-key key)))

(defmulti ui-key
  "Hiccup UI"
  (fn [key _schema]
    (#'ig/normalize-key key)))

(defmulti schema-key
  "Malli Schema"
  (fn [key]
    (#'ig/normalize-key key)))
