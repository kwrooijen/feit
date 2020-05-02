(ns rooij.system.middleware
  (:require
   [integrant-tools.core :as it]
   [taoensso.timbre :as timbre]
   [rooij.system.core :as system]
   [rooij.state :as state]))

(defn path
  ([entity-key component-key]
   [:scene/entities entity-key
    :entity/components component-key
    :component/middleware])
  ([entity-key component-key ticker]
   [:scene/entities entity-key
    :entity/components component-key
    :component/middleware ticker]))

(defn init [{:middleware/keys [key init] :as middleware}]
  (assoc middleware :middleware/fn (init key middleware)))

(defn add!
  ([{:context/keys [scene-key entity-key component-key]} middleware]
   (add! scene-key entity-key component-key middleware {}))
  ([{:context/keys [scene-key entity-key component-key]} middleware opts]
   (add! scene-key entity-key component-key middleware opts))
  ([scene-key entity-key component-key middleware-key opts]
   (swap! (state/get-scene-post-events scene-key) conj
          {:add/path (path entity-key component-key)
           :add/key middleware-key
           :add/system (init (merge (it/find-derived-value @state/system middleware-key) opts))
           :event/type :add/system})))

(defn remove!
  ([{:context/keys [scene-key entity-key component-key]} middleware]
   (remove! scene-key entity-key component-key middleware))
  ([scene-key entity-key component-key middleware-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path (path entity-key component-key)
           :remove/key middleware-key
           :event/type :remove/system})))

(defmethod system/init-key :rooij/middleware [k opts]
  (timbre/debug ::init-key opts)
  (assoc opts
         :middleware/init (system/get-init-key k)
         :middleware/priority (:middleware/priority opts 0)))
