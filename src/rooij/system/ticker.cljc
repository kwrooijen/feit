(ns rooij.system.ticker
  (:require
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.state :refer [get-scene]]
   [rooij.system.core :as system]
   [rooij.util :refer [top-key]]
   [taoensso.timbre :as timbre]))

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

(defn init [{:ticker/keys [key opts] :as ticker}]
  (-> ticker
      (assoc :ticker/fn (ig/init-key key opts))
      (update :ticker/subs meta-merge (:ticker/subs opts))))

(defn remove!
  ([{:context/keys [scene-key entity-key component]} ticker]
   (remove! scene-key entity-key component ticker))
  ([scene-key entity-key component ticker]
   (swap! (get-scene scene-key)
          update-in (path entity-key component) dissoc ticker)))

(defmethod system/init-key :rooij/ticker [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :ticker/opts opts
         :ticker/key (top-key k)
         :ticker/init (system/get-init-key k)
         :ticker/fn nil))
