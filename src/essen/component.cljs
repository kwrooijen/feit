(ns essen.component
  (:require
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]))

(defn path
  ([entity]
   [:scene/entities entity
    :entity/components])
  ([entity component]
   [:scene/entities entity
    :entity/components component]))

(defn init-process [k opts]
  (vec->map [] :some/key)
  (-> opts
      (assoc :component/key (last k)
             :component/state (ig/init-key k opts))
      (update :component/tickers vec->map :ticker/key)
      (update :component/handlers vec->map :handler/key)))
