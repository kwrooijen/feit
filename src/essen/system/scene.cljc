(ns essen.system.scene
  (:require
   [essen.system :as es]
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]))

(defmethod es/init-key :essen/scene [k opts]
  (-> (ig/init-key k opts)
      (assoc :scene/key (last k))))
