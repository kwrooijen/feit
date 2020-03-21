(ns essen.system.ticker
  (:require
   [essen.util :refer [top-key]]
   [essen.system :as es]
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
   (let [context {:context/scene scene
                  :context/entity entity}
         opts (merge opts context)]
     (swap! (get-scene scene)
            assoc-in
            (path entity component ticker)
            {:ticker/key ticker
             :ticker/fn (ig/init-key ticker opts)}))))

(defn remove!
  ([{:context/keys [scene entity component]} ticker]
   (remove! scene entity component ticker))
  ([scene entity component ticker]
   (swap! (get-scene scene)
          update-in (path entity component) dissoc ticker)))

(defmethod es/init-key :essen/ticker [k opts]
  (assoc opts
         :ticker/key (top-key k)
         :ticker/fn (ig/init-key k opts)))
