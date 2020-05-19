(ns feit.error
  (:require
   #?(:clj [clojure.pprint :refer [pprint]]
      :cljs [cljs.pprint :refer [pprint]])
   [taoensso.timbre :as timbre]))

(defn- pp [v]
  (with-out-str (pprint v)))

(defn translate-error [e]
  (cond
    (= (:type e) :malli.core/invalid-schema)
    :malli.core/invalid-schema

    (= (:reason e) :feit.system.scene/no-initial-scene)
    :feit.system.scene/no-initial-scene

    (= (:reason e) :integrant.core/build-threw-exception)
    :integrant.core/build-threw-exception))

(defmulti handle-error
  (comp translate-error
        ex-data))

(defmethod handle-error :malli.core/invalid-schema [e]
  (timbre/error "There was an eror in the Malli schema:"
                (-> (ex-data e) :data :schema))
  (throw e))

(defmethod handle-error :integrant.core/build-threw-exception [e]
  (timbre/error (str "The following key threw an error during initialization: \n\n"
                     ;; TODO How do you get .-cause in Clojure?
                     (.-cause e) "\n\n"
                     (:key (ex-data e)) "\n\n" "With the following opts: \n"
                     (pp (:value (ex-data e))))))

(defmethod handle-error :feit.system.scene/no-initial-scene [e]
  (timbre/error ::no-initial-scene (str "Configuration is missing `:feit/initial-scenen` key."))
  (throw e))

(defmethod handle-error :default [e]
  (timbre/error ::unhandled-exception (str (ex-data e)))
  nil)
