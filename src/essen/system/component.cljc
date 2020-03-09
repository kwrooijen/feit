(ns essen.system.component
  (:require
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]
   [essen.system :as es]))

(defn path
  ([entity]
   [:scene/entities entity
    :entity/components])
  ([entity component]
   [:scene/entities entity
    :entity/components component]))

(defmethod es/init-key :essen/component [k opts]
  (-> opts
      (select-keys [:component/tickers :component/handlers])
      (assoc :component/key (last k)
             :component/state (ig/init-key k opts))
      (update :component/tickers vec->map :ticker/key)
      (update :component/handlers vec->map :handler/key)))
