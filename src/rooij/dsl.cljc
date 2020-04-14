 (ns rooij.dsl
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]
   [rooij.util :refer [top-key]]
   [rooij.config]))

(defn new-child-key [k]
  (if (vector? k)
    k
    (make-child k)))

(def current-key
  (comp :current-key
        meta))

(defn ->composite-key [k ck]
  (if (vector? k)
    k
    [ck k]))

(defn- system [config k system-key]
  (with-meta
    (meta-merge config {(->composite-key k system-key) {}})
    {:current-key [system-key k]}))

(defn scene
  ([scene-key] (scene  {} scene-key))
  ([config scene-key]
   (system config scene-key :rooij/scene)))

(defn entity
  ([entity-key] (entity {} entity-key))
  ([config entity-key]
   (system config entity-key :rooij/entity)))

(defn component
  ([component-key] (component {} component-key))
  ([config component-key]
   (system config component-key :rooij/component)))

(defn handler
  ([handler-key] (handler {} handler-key))
  ([config handler-key]
   (system config handler-key :rooij/handler)))

(defn reactor
  ([reactor-key] (reactor {} reactor-key))
  ([config reactor-key]
   (system config reactor-key :rooij/reactor)))

(defn ticker
  ([ticker-key] (ticker {} ticker-key))
  ([config ticker-key]
   (system config ticker-key :rooij/ticker)))

(defn middleware
  ([middleware-key] (middleware {} middleware-key))
  ([config middleware-key]
   (system config middleware-key :rooij/middleware)))

(defn add-entity
  ([config entity-key]
   (add-entity config entity-key {}))
  ([config entity-key entity-config]
   (let [scene-key (current-key config)
         entity-key (new-child-key entity-key)
         entity (merge entity-config {:entity/ref (ig/ref (top-key entity-key))})]
     (when-not (#{:rooij/scene} (first scene-key))
       (throw (ex-info "You can only add entities to scenes" {:reason ::invalid-config})))
     (meta-merge config
                 {scene-key {:scene/entities [entity]}}
                 {entity-key entity-config}))))
(defn add-component
  ([config component-key]
   (add-component config component-key {}))
  ([config component-key component-config]
   (let [entity-key (current-key config)
         component-key (new-child-key component-key)
         component (merge component-config {:component/ref (ig/ref (top-key component-key))})]
     (when-not (#{:rooij/entity} (first entity-key))
       (throw (ex-info "You can only add components to entities" {:reason ::invalid-config})))
     (meta-merge config
                 {entity-key {:entity/components [component]}}
                 {component-key component-config}))))

(defn add-handler
  ([config handler-key]
   (add-handler config handler-key {}))
  ([config handler-key handler-config]
   (let [component-key (current-key config)
         handler-key (new-child-key handler-key)
         handler (merge handler-config {:handler/ref (ig/ref (top-key handler-key))})]
     (when-not (#{:rooij/component} (first component-key))
       (throw (ex-info "You can only add handlers to entities" {:reason ::invalid-config})))
     (meta-merge config
                 {component-key {:component/handlers [handler]}}
                 {handler-key handler-config}))))

(defn add-ticker
  ([config ticker-key]
   (add-ticker config ticker-key {}))
  ([config ticker-key ticker-config]
   (let [component-key (current-key config)
         ticker-key (new-child-key ticker-key)
         ticker (merge ticker-config {:ticker/ref (ig/ref (top-key ticker-key))})]
     (when-not (#{:rooij/component} (first component-key))
       (throw (ex-info "You can only add tickers to entities" {:reason ::invalid-config})))
     (meta-merge config
                 {component-key {:component/tickers [ticker]}}
                 {ticker-key ticker-config}))))

(defn add-reactor
  ([config reactor-key]
   (add-reactor config reactor-key {}))
  ([config reactor-key reactor-config]
   (let [component-key (current-key config)
         reactor-key (new-child-key reactor-key)
         reactor (merge reactor-config {:reactor/ref (ig/ref (top-key reactor-key))})]
     (when-not (#{:rooij/component} (first component-key))
       (throw (ex-info "You can only add reactors to entities" {:reason ::invalid-config})))
     (meta-merge config
                 {component-key {:component/reactors [reactor]}}
                 {[:rooij/reactor reactor-key] reactor-config}))))

(defn add-middleware
  ([config middleware-key]
   (add-middleware config middleware-key {}))
  ([config middleware-key middleware-config]
   (let [handler-key (current-key config)
         middleware-key (new-child-key middleware-key)
         middleware (merge middleware-config {:middleware/ref (ig/ref (top-key middleware-key))})]
     (when-not (#{:rooij/handler} (first handler-key))
       (throw (ex-info "You can only add middlewares to entities" {:reason ::invalid-config})))
     (meta-merge config
                 {handler-key {:handler/middlewares [middleware]}}
                 {middleware-key middleware-config}))))

(defn ref-entity
  ([config entity-key]
   (ref-entity config entity-key {}))
  ([config entity-key entity-config]
   (let [scene-key (current-key config)
         entity (merge entity-config {:entity/ref (ig/ref (top-key entity-key))})]
     (when-not (keyword? entity-key)
       (throw (ex-info "entity-key must be a keyword" {:reason ::invalid-ref-entity-keyword})))
     (when-not (#{:rooij/scene} (first scene-key))
       (throw (ex-info "You can only add entities to scenes" {:reason ::invalid-config})))
     (meta-merge config {scene-key {:scene/entities [entity]}}))))

(defn ref-component
  ([config component-key]
   (ref-component config component-key {}))
  ([config component-key component-config]
   (let [entity-key (current-key config)
         component (merge component-config {:component/ref (ig/ref component-key)})]
     (when-not (keyword? component-key)
       (throw (ex-info "component-key must be a keyword" {:reason ::invalid-ref-component-keyword})))
     (when-not (#{:rooij/entity} (first entity-key))
       (throw (ex-info "You can only add components to entities" {:reason ::invalid-config})))
     (meta-merge config {entity-key {:entity/components [component]}}))))

(defn ref-handler
  ([config handler-key]
   (ref-handler config handler-key {}))
  ([config handler-key handler-config]
   (let [component-key (current-key config)
         handler (merge handler-config {:handler/ref (ig/ref handler-key)})]
     (when-not (keyword? handler-key)
       (throw (ex-info "handler-key must be a keyword" {:reason ::invalid-ref-handler-keyword})))
     (when-not (#{:rooij/component} (first component-key))
       (throw (ex-info "You can only add handlers to components" {:reason ::invalid-config})))
     (meta-merge config {component-key {:component/handlers [handler]}}))))

(defn initial-scene
  ([config]
   (let [[key-type scene-key] (current-key config)]
     (when-not (#{:rooij/scene} key-type)
       (throw (ex-info "You can only mark scenes as intial-scene"
                       {:reason ::invalid-scene-key})))
     (initial-scene config scene-key)))
  ([config scene]
   (assoc config :rooij/initial-scene scene)))

(defn config-key [config]
  ;; This is to cover the case of:
  ;; [:rooij/entity :some/entity]
  ;; [:rooij/entity [:parent/entity :some/entity]]
  ;; Maybe we should change `:some/entity` to `[:rooij/entity :some/entity]`
  (let [current (current-key config)
        k (second current)]
    (if (keyword? k)
      current
      k)))

(defn add-opts [config opts]
  (update config (config-key config) meta-merge opts))

(defn persistent [config]
  (when-not (#{:rooij/component} (first (current-key config)))
    (throw (ex-info "You can only make components persistent"
                    {:reason ::invalid-persistent-key})))
  (update config (config-key config) assoc :component/persistent true))

(defn save! [config]
  (rooij.config/merge-user! config))
