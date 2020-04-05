(ns essen.methods
  (:require
   #?(:clj [clojure.pprint :refer [pprint]]
      :cljs [cljs.pprint :refer [pprint]])
   [malli.core :as m]
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

(defmethod schema-key :default [_] nil)

(defn invalid-schema-error [key schema value]
  (str "\n"
       "Schema error on key " key
       "\n\n"
       (with-out-str (pprint (:value (m/explain schema value))))
       "\n\n"
       (with-out-str (pprint (:errors (m/explain schema value))))))

(defn assert-schema-key [system key value]
  (when-let [schema (schema-key key)]
    (when-not (m/validate schema value)
      (throw (ex-info (invalid-schema-error key schema value)
                      (m/explain schema value))))))
