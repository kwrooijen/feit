(ns rooij.api
  (:require
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [make-child]]
   [integrant.core :as ig]
   [rooij.state :as state]
   [rooij.system.component :as component]
   [rooij.system.entity :as entity]
   [rooij.system.handler :as handler]
   [rooij.system.middleware :as middleware]
   [rooij.system.reactor :as reactor]
   [rooij.system.ticker :as ticker]
   [rooij.util :refer [->context top-key]]))

(defn scenes
  "Get all current running scenes as a set."
  []
  (-> (state/get-scenes)
      (keys)
      (set)))

(defn emit!
  "Emit a event with `content` to an `entity`'s `handler` in `scene`"
  ([context handler-key]
   (emit! context handler-key nil []))
  ([context handler-key content]
   (emit! context handler-key content []))
  ([{:context/keys [scene-key entity-key] :as context} handler-key content excludes]
   (swap! (state/get-scene-events scene-key) conj
          {:event/entity entity-key
           :event/handler handler-key
           :event/content content
           :event/excludes excludes})
   context))

(defn- apply-query-filters [filters v]
  (filter (fn [[_ state]]
            (every?
             (fn [[fk fp]]
               (fp (get state fk)))
             filters))
          v))

(defn query
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  ([scene-key entity-key]
   (query scene-key entity-key []))
  ([scene-key entity-key filters]
   (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
        (map (fn [[k v]] [k (:entity/state v)]))
        (apply-query-filters filters)
        (into {}))))

(defn query-keys
  ([scene-key entity-key]
   (query-keys scene-key entity-key []))
  ([scene-key entity-key filters]
   (keys (query scene-key entity-key filters))))

(defn select
  ([scene-key entity-key]
   (select scene-key entity-key []))
  ([scene-key entity-key filters]
   (get (query scene-key entity-key filters) entity-key)))

(defn select-value
  ([scene-key entity-key]
   (select-value scene-key entity-key []))
  ([scene-key entity-key filters]
   (-> (select scene-key entity-key filters)
       (vals)
       (first))))

(defn transition-scene
  "Transition from `scene-from` to `scene-to`. Halts `scene-from` before
  initializing `scene-to`."
  [scene-from scene-to]
  (swap! (state/get-scene-post-events scene-from) conj
         {:scene/key scene-to
          :event/type :scene/start!})
  (swap! (state/get-scene-post-events scene-from) conj
         {:scene/key scene-from
          :event/type :scene/halt!}))

(defn restart-scene
  [scene-key]
  (swap! (state/get-scene-post-events scene-key) conj
         {:scene/key scene-key
          :event/type :scene/halt!})
  (swap! (state/get-scene-post-events scene-key) conj
         {:scene/key scene-key
          :event/type :scene/start!}))

(defn add-entity!
  [config]
  (let [entity-key (-> config meta :entity/last last)
        scene-key (-> config meta :scene/last last top-key)
        entity-ref (it/find-derived-value @state/system entity-key)
        entity-key (if (:entity/dynamic entity-ref)
                     (make-child (top-key entity-key))
                     (top-key entity-key))
        entity (-> (get-in config (:entity/last (meta config)))
                   (assoc :entity/ref entity-ref)
                   (->> (entity/preprocess-entity (->context scene-key entity-key) entity-key)
                        (entity/postprocess-entity)))]
    (swap! (state/get-scene-post-events scene-key) conj
           {:add/path [:scene/entities]
            :add/key entity-key
            :add/system entity
            :event/type :add/system})
    config))

(defn add-component!
  [config]
  (let [config-meta (meta config)
        component-key (-> config-meta :component/last last top-key)
        scene-key (-> config-meta :scene/last last top-key)
        entity-key (-> config-meta :entity/last last top-key)
        component-ref (it/find-derived-value @state/system component-key)
        component (-> (get-in config (:component/last config-meta))
                   (assoc :component/ref component-ref)
                   (->> (component/preprocess-component (->context scene-key entity-key component-key) component-key)))
        path [:scene/entities entity-key
              :entity/components]]
    (swap! (state/get-scene-post-events scene-key) conj
           {:add/path path
            :add/key component-key
            :add/system component
            :event/type :add/system})
    config))

(defn add-handler!
  [config]
  (let [config-meta (meta config)
        handler-key (-> config-meta :handler/last last)
        scene-key (-> config-meta :scene/last last top-key)
        entity-key (-> config-meta :entity/last last top-key)
        component-key (-> config-meta :component/last last top-key)
        handler-ref (it/find-derived-value @state/system handler-key)
        handler (-> (get-in config (:handler/last config-meta))
                    (assoc :handler/ref handler-ref)
                    (->> (handler/preprocess-handler (->context scene-key entity-key component-key) handler-key)))
        path [:scene/entities entity-key
              :entity/components component-key
              :component/handlers]]
    (swap! (state/get-scene-post-events scene-key) conj
           {:add/path path
            :add/key handler-key
            :add/system handler
            :event/type :add/system})
    config))

(defn add-ticker!
  [config]
  (let [config-meta (meta config)
        ticker-key (-> config-meta :ticker/last last)
        scene-key (-> config-meta :scene/last last top-key)
        entity-key (-> config-meta :entity/last last top-key)
        component-key (-> config-meta :component/last last top-key)
        ticker-ref (it/find-derived-value @state/system ticker-key)
        ticker (-> (get-in config (:ticker/last config-meta))
                   (assoc :ticker/ref ticker-ref)
                   (->> (ticker/preprocess-ticker (->context scene-key entity-key component-key) ticker-key)))
        path [:scene/entities entity-key
              :entity/components component-key
              :component/tickers]]
    (swap! (state/get-scene-post-events scene-key) conj
           {:add/path path
            :add/key ticker-key
            :add/system ticker
            :event/type :add/system})
    config))

(defn add-reactor!
  [config]
  (let [config-meta (meta config)
        reactor-key (-> config-meta :reactor/last last)
        scene-key (-> config-meta :scene/last last top-key)
        entity-key (-> config-meta :entity/last last top-key)
        component-key (-> config-meta :component/last last top-key)
        reactor-ref (it/find-derived-value @state/system reactor-key)
        reactor (-> (get-in config (:reactor/last config-meta))
                   (assoc :reactor/ref reactor-ref)
                   (->> (reactor/preprocess-reactor (->context scene-key entity-key component-key) reactor-key)))
        path [:scene/entities entity-key
              :entity/components component-key
              :component/reactors]]
    (swap! (state/get-scene-post-events scene-key) conj
           {:add/path path
            :add/key reactor-key
            :add/system reactor
            :event/type :add/system})
    config))

(defn add-middleware!
  [config]
  (let [config-meta (meta config)
        middleware-key (-> config-meta :middleware/last last)
        scene-key (-> config-meta :scene/last last top-key)
        entity-key (-> config-meta :entity/last last top-key)
        component-key (-> config-meta :component/last last top-key)
        middleware-ref (it/find-derived-value @state/system middleware-key)
        middleware (-> (get-in config (:middleware/last config-meta))
                       (assoc :middleware/ref middleware-ref)
                       (->> (middleware/preprocess-middleware (->context scene-key entity-key component-key) middleware-key)))
        path [:scene/entities entity-key
              :entity/components component-key
              :component/middlewares]]
    (swap! (state/get-scene-post-events scene-key) conj
           {:add/path path
            :add/key middleware-key
            :add/system middleware
            :event/type :add/system})
    config))
