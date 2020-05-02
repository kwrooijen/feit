(ns rooij.system.ticker
  (:require
   [integrant-tools.core :as it]
   [rooij.state :as state]
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

(defn init [{:ticker/keys [key init] :as ticker}]
  (assoc ticker :ticker/fn (init key ticker)))

(defn add!
  ([{:context/keys [scene-key entity-key component-key]} ticker]
   (add! scene-key entity-key component-key ticker {}))
  ([{:context/keys [scene-key entity-key component-key]} ticker opts]
   (add! scene-key entity-key component-key ticker opts))
  ([scene-key entity-key component-key ticker-key opts]
   (swap! (state/get-scene-post-events scene-key) conj
          {:add/path (path entity-key component-key)
           :add/key ticker-key
           :add/system (init (merge (it/find-derived-value @state/system ticker-key) opts))
           :event/type :add/system})))

(defn remove!
  ([{:context/keys [scene-key entity-key component-key]} ticker]
   (remove! scene-key entity-key component-key ticker))
  ([scene-key entity-key component-key ticker-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path (path entity-key component-key)
           :remove/key ticker-key
           :event/type :remove/system})))

(defmethod system/init-key :rooij/ticker [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :ticker/key (top-key k)
         :ticker/init (system/get-init-key k)
         :ticker/fn nil))
