(ns essen.system.component
  (:require
   [integrant.core :as ig]
   [essen.util :refer [vec->map]]
   [essen.state :refer [persistent-components]]
   [essen.system :as es]))

(defn path
  ([entity]
   [:scene/entities entity
    :entity/components])
  ([entity component]
   [:scene/entities entity
    :entity/components component]))

(defmethod es/init-key :essen/component [k opts]
  (or (get @persistent-components (last k))
      (-> opts
          (select-keys [:component/tickers :component/handlers :component/reactors])
          (assoc :component/key (last k)
                 :component/state (ig/init-key k opts)
                 :component/persistent (:persistent (meta k)))
          (update :component/tickers vec->map :ticker/key)
          (update :component/handlers vec->map :handler/key)
          (update :component/reactors vec->map :reactor/key))))
