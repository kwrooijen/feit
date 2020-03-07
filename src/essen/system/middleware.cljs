(ns essen.system.middleware
  (:require
   [essen.system :as es]
   [essen.state :refer [get-scene]]
   [integrant.core :as ig]))

(defn path
  ([entity component handler]
   [:scene/entities entity
    :entity/components component
    :component/handlers handler
    :handler/middleware])
  ([entity component handler middleware]
   [:scene/entities entity
    :entity/components component
    :component/handlers handler
    :handler/middleware middleware]))

(defn add!
  ([{:context/keys [scene entity component]} handler middleware opts]
   (add! scene entity component handler middleware opts))
  ([scene entity component handler middleware opts]
   (swap! (get-scene scene)
          assoc-in
          (path entity component handler middleware)
          {:middleware/key middleware
           :middleware/fn (ig/init-key middleware opts)})))

(defn remove!
  ([{:context/keys [scene entity component]} handler middleware]
   (remove! scene entity component handler middleware))
  ([scene entity component handler middleware]
   (swap! (get-scene scene)
          update-in
          (path entity component handler)
          dissoc middleware)))

(defmethod es/init-key :essen/middleware [k opts]
  (assoc opts
         :middleware/key (last k)
         :middleware/fn (ig/init-key k opts)))