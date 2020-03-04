(ns essen.ticker
  (:require
   [essen.state :refer [get-scene]]
   [integrant.core :as ig]))

(defn path
  ([entity component]
   [:scene/entities entity
    :entity/components component
    :component/tickers])
  ([entity component ticker]
   [:scene/entities entity
    :entity/components component
    :component/tickers ticker]))

(defn add!
  ([{:context/keys [scene entity component]} ticker opts]
   (add! scene entity component ticker opts))
  ([scene entity component ticker opts]
   (swap! (get-scene scene)
          assoc-in
          (path entity component ticker)
          {:ticker/key ticker
           :ticker/fn (ig/init-key ticker opts)})))

(defn remove!
  ([{:context/keys [scene entity component]} ticker]
   (remove! scene entity component ticker))
  ([scene entity component ticker]
   (swap! (get-scene scene)
          update-in
          (path entity component)
         dissoc ticker)))
