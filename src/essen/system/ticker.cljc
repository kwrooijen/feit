(ns essen.system.ticker
  (:require
   [essen.util :refer [top-key]]
   [essen.system :as es]
   [essen.state :refer [get-scene]]
   [integrant.core :as ig]))

(defn path
  ([entity-key component-key]
   [:scene/entities entity-key
    :entity/components component-key
    :component/tickers])
  ([entity-key component-key ticker]
   [:scene/entities entity-key
    :entity/components component-key
    :component/tickers ticker]))

(defn add!
  ([{:context/keys [scene-key entity-key component]} ticker opts]
   (add! scene-key entity-key component ticker opts))
  ([scene-key entity-key component ticker opts]
   (let [context {:context/scene-key scene-key
                  :context/entity-key entity-key}
         opts (merge opts context)]
     (swap! (get-scene scene-key)
            assoc-in
            (path entity-key component ticker)
            {:ticker/key ticker
             :ticker/fn (ig/init-key ticker opts)}))))

(defn remove!
  ([{:context/keys [scene-key entity-key component]} ticker]
   (remove! scene-key entity-key component ticker))
  ([scene-key entity-key component ticker]
   (swap! (get-scene scene-key)
          update-in (path entity-key component) dissoc ticker)))

(defmethod es/init-key :essen/ticker [k opts]
  (assoc opts
         :ticker/key (top-key k)
         :ticker/fn (ig/init-key k opts)))
