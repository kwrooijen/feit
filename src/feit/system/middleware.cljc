(ns feit.system.middleware
  (:require
   [meta-merge.core :refer [meta-merge]]
   [feit.system.core :as system]
   [feit.util :refer [->context map-kv]]
   [taoensso.timbre :as timbre]))

(defmethod system/init-key :feit/middleware [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :middleware/init (system/get-init-key k {:required? true})
         :middleware/priority (:middleware/priority opts 0)))

(def context-keys
  [:context/scene-key
   :context/entity-key
   :context/component-key])

(defn preprocess-middleware [context middleware-key middleware-opts]
  (-> middleware-opts
      (->> (meta-merge (:middleware/ref middleware-opts)))
      (dissoc :middleware/ref)
      (->> (merge (select-keys context context-keys)))
      (assoc :middleware/key middleware-key)
      (as-> $ (assoc $ :middleware/fn ((:middleware/init $) middleware-key $)))))

(defn preprocess-middlewares [scene-key entity-key component-key middleware]
  (map-kv #(preprocess-middleware (->context scene-key entity-key component-key) %1 %2) middleware))
