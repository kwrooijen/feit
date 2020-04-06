(ns essen.methods
  (:require
   [essen.interface.core :refer [registry]]
   #?(:clj [clojure.pprint :refer [pprint]]
      :cljs [cljs.pprint :refer [pprint]])
   [malli.core :as m]
   [malli.error :as me]
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
  (str "Schema error on key " key
       "\n\n"
       (with-out-str (pprint (me/humanize (m/explain schema value {:registry @registry}))))))

(defn assert-schema-key [_system key value]
  (when-let [schema (schema-key key)]
    (when-not (m/validate schema value {:registry @registry})
      (throw (ex-info (invalid-schema-error key schema value)
                      (m/explain schema value {:registry @registry}))))))
