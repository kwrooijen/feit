(ns essen.system.scene
  (:require
   [essen.system :as es]
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]))

(defmethod es/init-key :essen/scene [k opts]
  (-> opts
      (assoc :scene/key (last k))))
