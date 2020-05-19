(ns feit.api
  (:require
   [clojure.walk :refer [postwalk]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [make-child]]
   [integrant.core :as ig]
   [feit.core.state :as state]
   [feit.system.component :as component]
   [feit.system.entity :as entity]
   [feit.system.handler :as handler]
   [feit.system.middleware :as middleware]
   [feit.system.reactor :as reactor]
   [feit.system.ticker :as ticker]
   [feit.core.util :refer [->context top-key]]))

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

(defn entities
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  ([scene-key entity-key]
   (entities scene-key entity-key []))
  ([scene-key entity-key filters]
   (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
        (map (fn [[k v]] [k (:entity/state v)]))
        (apply-query-filters filters)
        (into {}))))

(defn entities-keys
  "Get all entity keys that derive from `entity-key` in the scene
  `scene-key`. Optionally you can apply `filters` in the form:

  ```clojure
  [[:some/component #(> (:component/points %) 50]]
  ```

  Which will return any entity keys that have the component
  `:some/component`, where its state must contain a key
  `:component/points`, which is greater than 50."
  ([scene-key entity-key]
   (entities-keys scene-key entity-key []))
  ([scene-key entity-key filters]
   (keys (entities scene-key entity-key filters))))

(defn entity
  ([scene-key entity-key]
   (entity scene-key entity-key []))
  ([scene-key entity-key filters]
   (get (entities scene-key entity-key filters) entity-key)))

(defn entity-value
  ([scene-key entity-key]
   (entity-value scene-key entity-key []))
  ([scene-key entity-key filters]
   (-> (entity scene-key entity-key filters)
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

(defn- resolve-refs
  [config]
  (let [system @state/system]
    (postwalk
     (fn [m]
       (cond
         (:entity/ref m) (assoc m :entity/ref (it/find-derived-value system (:entity/key m)))
         (:component/ref m) (assoc m :component/ref (it/find-derived-value system (:component/key m)))
         (:handler/ref m) (assoc m :handler/ref (it/find-derived-value system (:handler/key m)))
         (:ticker/ref m) (assoc m :ticker/ref (it/find-derived-value system (:ticker/key m)))
         (:reactor/ref m) (assoc m :reactor/ref (it/find-derived-value system (:reactor/key m)))
         (:middleware/ref m) (assoc m :middleware/ref (it/find-derived-value system (:middleware/key m)))
         :else m))
     config)))

(defn add-entity!
  [config]
  (let [config-meta (meta config)
        config (resolve-refs config)
        entity-key (-> config-meta :entity/last last)
        scene-key (-> config-meta :scene/last last top-key)
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
        config (resolve-refs config)
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
        config (resolve-refs config)
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
        config (resolve-refs config)
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
        config (resolve-refs config)
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
        config (resolve-refs config)
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

(defn remove-entity!
  ([{:context/keys [scene-key entity-key]}]
   (remove-entity! scene-key entity-key))
  ([scene-key entity-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path [:scene/entities]
           :remove/key entity-key
           :remove/system-type :system/entity
           :event/type :remove/system})))


(defn remove-component!
  ([{:context/keys [scene-key entity-key component-key]}]
   (remove-component! scene-key entity-key component-key))
  ([scene-key entity-key component-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path [:scene/entities entity-key
                         :entity/components]
           :remove/key component-key
           :remove/system-type :system/component
           :event/type :remove/system})))

(defn remove-handler!
  ([{:context/keys [scene-key entity-key component-key]} handler]
   (remove-handler! scene-key entity-key component-key handler))
  ([scene-key entity-key component-key handler-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path [:scene/entities entity-key
                         :entity/components component-key
                         :component/handlers]
           :remove/key handler-key
           :remove/system-type :system/handler
           :event/type :remove/system})))

(defn remove-ticker!
  ([{:context/keys [scene-key entity-key component-key]} ticker]
   (remove-ticker! scene-key entity-key component-key ticker))
  ([scene-key entity-key component-key ticker-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path [:scene/entities entity-key
                         :entity/components component-key
                         :component/tickers]
           :remove/key ticker-key
           :remove/system-type :system/ticker
           :event/type :remove/system})))

(defn remove-reactor!
  ([{:context/keys [scene-key entity-key component-key]} reactor]
   (remove-reactor! scene-key entity-key component-key reactor))
  ([scene-key entity-key component-key reactor-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path [:scene/entities entity-key
                         :entity/components component-key
                         :component/reactors]
           :remove/key reactor-key
           :remove/system-type :system/reactor
           :event/type :remove/system})))

(defn remove-middleware!
  ([{:context/keys [scene-key entity-key component-key]} middleware]
   (remove-middleware! scene-key entity-key component-key middleware))
  ([scene-key entity-key component-key middleware-key]
   (swap! (state/get-scene-post-events scene-key) conj
          {:remove/path [:scene/entities entity-key
                         :entity/components component-key
                         :component/middlewares]
           :remove/key middleware-key
           :remove/system-type :system/middleware
           :event/type :remove/system})))
