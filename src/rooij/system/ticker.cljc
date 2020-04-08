(ns rooij.system.ticker
  (:require
   [taoensso.timbre :as timbre]
   [rooij.util :refer [top-key]]
   [rooij.system.core :as system]
   [rooij.state :refer [get-scene]]
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

(defmethod system/init-key :rooij/ticker [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :ticker/key (top-key k)
         :ticker/fn (ig/init-key k opts)))
