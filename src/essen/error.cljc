(ns essen.error
  (:require
   [taoensso.timbre :as timbre]))

(defmulti handle-error
  (comp :type
        ex-data))

(defmethod handle-error :malli.core/invalid-schema [e]
  (timbre/error "There was an eror in the Malli schema:"
                (-> (ex-data e) :data :schema))
  (throw e))

(defmethod handle-error :default [_] nil)
