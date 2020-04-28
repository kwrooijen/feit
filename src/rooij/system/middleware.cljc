(ns rooij.system.middleware
  (:require
   [taoensso.timbre :as timbre]
   [rooij.system.core :as system]
   [rooij.util :refer [top-key]]
   [rooij.state :as state]
   [integrant.core :as ig]))

(defn path
  ([entity-key component-key]
   [:scene/entities entity-key
    :entity/components component-key
    :component/middleware])
  ([entity-key component-key ticker]
   [:scene/entities entity-key
    :entity/components component-key
    :component/middleware ticker]))

(defn add!
  ([{:context/keys [scene-key entity-key component-key]} ticker]
   (add! scene-key entity-key component-key ticker))
  ([scene-key entity-key component-key ticker-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:add/path (path entity-key component-key)
           :add/key ticker-key
           :event/type :add/ticker})))

(defn remove!
  ([{:context/keys [scene-key entity-key component-key]} ticker]
   (remove! scene-key entity-key component-key ticker))
  ([scene-key entity-key component-key ticker-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path (path entity-key component-key)
           :remove/key ticker-key
           :event/type :remove/ticker})))

(defmethod system/init-key :rooij/middleware [k opts]
  (timbre/debug ::init-key opts)
  (-> opts
      (assoc :middleware/key (top-key k)
             :middleware/fn (ig/init-key k opts))))

(defn init [{:middleware/keys [key opts] :as middleware}]
  (-> middleware
      (assoc :middleware/fn (ig/init-key key opts))
      (update :middleware/handlers concat (:middleware/handlers opts))))
