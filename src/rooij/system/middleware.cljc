(ns rooij.system.middleware
  (:require [integrant-tools.core :as it]
            [meta-merge.core :refer [meta-merge]]
            [rooij.state :as state]
            [rooij.system.core :as system]
            [rooij.util :refer [->context map-kv]]
            [taoensso.timbre :as timbre]))

(defmethod system/init-key :rooij/middleware [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :middleware/init (system/get-init-key k {:required? true})
         :middleware/priority (:middleware/priority opts 0)))

(def context-keys
  [:context/scene-key
   :context/entity-key
   :context/component-key])

(defn path
  ([entity-key component-key]
   [:scene/entities entity-key
    :entity/components component-key
    :component/middlewares])
  ([entity-key component-key ticker]
   [:scene/entities entity-key
    :entity/components component-key
    :component/middlewares ticker]))

(defn preprocess-middleware [context middleware-key middleware-opts]
  (-> middleware-opts
      (->> (meta-merge (:middleware/ref middleware-opts)))
      (dissoc :middleware/ref)
      (->> (merge (select-keys context context-keys)))
      (assoc :middleware/key middleware-key)
      (as-> $ (assoc $ :middleware/fn ((:middleware/init $) middleware-key $)))))

(defn preprocess-middlewares [scene-key entity-key component-key middleware]
  (map-kv #(preprocess-middleware (->context scene-key entity-key component-key) %1 %2) middleware))

(defn remove!
  ([{:context/keys [scene-key entity-key component-key]} middleware]
   (remove! scene-key entity-key component-key middleware))
  ([scene-key entity-key component-key middleware-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path (path entity-key component-key)
           :remove/key middleware-key
           :event/type :remove/system})))
