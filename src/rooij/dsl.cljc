(ns rooij.dsl
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]
   [rooij.util :refer [bottom-key top-key ->vec]]
   [rooij.config]))

(defn error-map [system-key config system-config]
  {::system-key system-key
   ::config config
   ::system-config system-config})

(defn valid-system-key? [system-key]
  (or (qualified-keyword? system-key)
      (vector? system-key)))

(defn error-msg-system-key [system-key]
  (str "Error trying to add reference.\n"
       "Invalid system-key: " system-key "\n\n"
       "Should be a qualified-keyword or a vector of qualified keywords"))

(defn- ref-system-pre [config system-key system-config]
  (let [context (error-map config system-key system-config)]
    (cond
      (not (valid-system-key? system-key))
      (throw (ex-info (error-msg-system-key system-key) context))
      :else true)))

(defn- add-hierarchy [config k]
  (if (= (top-key k) (bottom-key k))
    config
    (update config :keyword/hierarchy meta-merge {(top-key k) [(bottom-key k)]})))

(defn- ref-system [config system-key system-config parent collection child]
  (let [parent-path (conj ((keyword parent "last") (meta config)) (keyword parent collection))
        component-id (top-key system-key)
        full-path (conj parent-path component-id)
        system-config (-> system-config
                          (assoc (keyword child "ref") (ig/ref (bottom-key system-key)))
                          (add-hierarchy system-key))]
    (-> config
        (update-in full-path meta-merge system-config)
        (vary-meta assoc (keyword child "last") full-path))))

(defn scene
  ([scene-key]
   (scene {} scene-key {}))
  ([key-or-config key-or-opts]
   (if (map? key-or-config)
     (scene key-or-config key-or-opts {})
     (scene {} key-or-config key-or-opts)))
  ([config scene-key scene-opts]
   {:pre [(qualified-keyword? scene-key)]}
   (-> config
       (meta-merge {[:rooij/scene scene-key] scene-opts})
       (vary-meta assoc :scene/last [[:rooij/scene scene-key]]))))

(defn entity
  ([entity-key]
   (entity {} entity-key {}))
  ([key-or-config key-or-opts]
   (if (map? key-or-config)
     (entity key-or-config key-or-opts {})
     (entity {} key-or-config key-or-opts)))
  ([config entity-key entity-opts]
   {:pre [(qualified-keyword? entity-key)]}
   (-> config
       (meta-merge {[:rooij/entity entity-key] entity-opts})
       (vary-meta assoc :entity/last [[:rooij/entity entity-key]]))))

(defn component
  ([component-key]
   (component {} component-key {}))
  ([key-or-config key-or-opts]
   (if (map? key-or-config)
     (component key-or-config key-or-opts {})
     (component {} key-or-config key-or-opts)))
  ([config component-key component-opts]
   {:pre [(qualified-keyword? component-key)]}
   (-> config
       (meta-merge {[:rooij/component component-key] component-opts})
       (vary-meta assoc :component/last [[:rooij/component component-key]]))))

(defn handler
  ([handler-key]
   (handler {} handler-key {}))
  ([key-or-config key-or-opts]
   (if (map? key-or-config)
     (handler key-or-config key-or-opts {})
     (handler {} key-or-config key-or-opts)))
  ([config handler-key handler-opts]
   {:pre [(qualified-keyword? handler-key)]}
   (-> config
       (meta-merge {[:rooij/handler handler-key] handler-opts})
       (vary-meta assoc :handler/last [[:rooij/handler handler-key]]))))

(defn ticker
  ([ticker-key]
   (ticker {} ticker-key {}))
  ([key-or-config key-or-opts]
   (if (map? key-or-config)
     (ticker key-or-config key-or-opts {})
     (ticker {} key-or-config key-or-opts)))
  ([config ticker-key ticker-opts]
   {:pre [(qualified-keyword? ticker-key)]}
   (-> config
       (meta-merge {[:rooij/ticker ticker-key] ticker-opts})
       (vary-meta assoc :ticker/last [[:rooij/ticker ticker-key]]))))

(defn reactor
  ([reactor-key]
   (reactor {} reactor-key {}))
  ([key-or-config key-or-opts]
   (if (map? key-or-config)
     (reactor key-or-config key-or-opts {})
     (reactor {} key-or-config key-or-opts)))
  ([config reactor-key reactor-opts]
   {:pre [(qualified-keyword? reactor-key)]}
   (-> config
       (meta-merge {[:rooij/reactor reactor-key] reactor-opts})
       (vary-meta assoc :reactor/last [[:rooij/reactor reactor-key]]))))

(defn middleware
  ([middleware-key]
   (middleware {} middleware-key {}))
  ([key-or-config key-or-opts]
   (if (map? key-or-config)
     (middleware key-or-config key-or-opts {})
     (middleware {} key-or-config key-or-opts)))
  ([config middleware-key middleware-opts]
   {:pre [(qualified-keyword? middleware-key)]}
   (-> config
       (meta-merge {[:rooij/middleware middleware-key] middleware-opts})
       (vary-meta assoc :middleware/last [[:rooij/middleware middleware-key]]))))

(defn keyboard
  ([keyboard-key]
   (keyboard {} keyboard-key {}))
  ([key-or-config key-or-opts]
   (if (map? key-or-config)
     (keyboard key-or-config key-or-opts {})
     (keyboard {} key-or-config key-or-opts)))
  ([config keyboard-key keyboard-opts]
   {:pre [(qualified-keyword? keyboard-key)]}
   (-> config
       (meta-merge {[:rooij/keyboard keyboard-key] keyboard-opts})
       (vary-meta assoc :keyboard/last [[:rooij/keyboard keyboard-key]]))))

(defn ref-entity
  ([config entity-key]
   (ref-entity config entity-key {}))
  ([config entity-key entity-opts]
   {:pre [(qualified-keyword? entity-key)]}
   (let [parent-path (conj (:scene/last (meta config)) :scene/entities)
         entity-id (if (:entity/dynamic entity-opts)
                     (make-child (top-key entity-key))
                     (top-key entity-key))
         full-path (conj parent-path entity-id)
         entity-opts (-> entity-opts
                         (assoc :entity/ref (ig/ref (bottom-key entity-key)))
                         (add-hierarchy entity-key))]
     (-> config
         (update-in full-path meta-merge entity-opts)
         (vary-meta assoc :entity/last full-path)))))

(defn ref-component
  ([config component-key]
   (ref-component config component-key {}))
  ([config component-key component-opts]
   {:pre [(ref-system-pre config component-key component-opts)]}
   (when-not (:entity/last (meta config)) (throw "Can only add components to entities."))
   (ref-system config component-key component-opts :entity :components :component)))

(defn ref-handler
  ([config handler-key]
   (ref-handler config handler-key {}))
  ([config handler-key handler-opts]
   {:pre [(ref-system-pre config handler-key handler-opts)]}
   (when-not (:component/last (meta config)) (throw "Can only add handlers to components."))
   (ref-system config handler-key handler-opts :component :handlers :handler)))

(defn ref-ticker
  ([config ticker-key]
   (ref-ticker config ticker-key {}))
  ([config ticker-key ticker-opts]
   {:pre [(ref-system-pre config ticker-key ticker-opts)]}
   (when-not (:component/last (meta config)) (throw "Can only add tickers to components."))
   (ref-system config ticker-key ticker-opts :component :tickers :ticker)))

(defn ref-reactor
  ([config reactor-key]
   (ref-reactor config reactor-key {}))
  ([config reactor-key reactor-opts]
   {:pre [(ref-system-pre config reactor-key reactor-opts)]}
   (when-not (:component/last (meta config)) (throw "Can only add reactors to components."))
   (ref-system config reactor-key reactor-opts :component :reactors :reactor)))

(defn ref-middleware
  ([config middleware-key]
   (ref-middleware config middleware-key {} []))
  ([config middleware-key middleware-opts]
   (ref-middleware config middleware-key middleware-opts []))
  ([config middleware-key middleware-opts handlers]
   {:pre [(ref-system-pre config middleware-key middleware-opts)]}
   (when-not (:component/last (meta config)) (throw "Can only add middlewares to components."))
   (ref-system config
               middleware-key
               (assoc middleware-opts :middleware/handlers handlers)
               :component :middlewares :middleware)))

(defn ref-keyboard-down
  ([config keyboard-key keyboard-down-key]
   (ref-keyboard-down config keyboard-key keyboard-down-key {}))
  ([config keyboard-key keyboard-down-key keyboard-opts]
   (when-not (:scene/last (meta config)) (throw "Can only add keyboards to scenes."))
   (ref-system config keyboard-key
               (merge keyboard-opts {:keyboard-down/key keyboard-down-key})
               :scene :keyboards :keyboard)))

(defn ref-keyboard-up
  ([config keyboard-key keyboard-up-key]
   (ref-keyboard-up config keyboard-key keyboard-up-key {}))
  ([config keyboard-key keyboard-up-key keyboard-opts]
   (when-not (:scene/last (meta config)) (throw "Can only add keyboards to scenes."))
   (ref-system config keyboard-key
               (merge keyboard-opts {:keyboard-up/key keyboard-up-key})
               :scene :keyboards :keyboard)))

(defn ref-keyboard-while-down
  ([config keyboard-key keyboard-while-down-key]
   (ref-keyboard-while-down config keyboard-key keyboard-while-down-key {}))
  ([config keyboard-key keyboard-while-down-key keyboard-opts]
   (when-not (:scene/last (meta config)) (throw "Can only add keyboards to scenes."))
   (ref-system config keyboard-key
               (merge keyboard-opts {:keyboard-while-down/key keyboard-while-down-key})
               :scene :keyboards :keyboard)))

(defn entity+ref
  ([config entity-key]
   (entity+ref config entity-key {}))
  ([config entity-key entity-opts]
   (-> config
       (entity entity-key entity-opts)
       (ref-entity entity-key))))

(defn component+ref
  ([config component-key]
   (component+ref config component-key {}))
  ([config component-key component-opts]
   (-> config
       (component component-key component-opts)
       (ref-component component-key))))

(defn handler+ref
  ([config handler-key]
   (handler+ref config handler-key {}))
  ([config handler-key handler-opts]
   (-> config
       (handler handler-key handler-opts)
       (ref-handler handler-key))))

(defn ticker+ref
  ([config ticker-key]
   (ticker+ref config ticker-key {}))
  ([config ticker-key ticker-opts]
   (-> config
       (ticker ticker-key ticker-opts)
       (ref-ticker ticker-key))))

(defn reactor+ref
  ([config reactor-key]
   (reactor+ref config reactor-key {}))
  ([config reactor-key reactor-opts]
   (-> config
       (reactor reactor-key reactor-opts)
       (ref-reactor reactor-key))))

(defn middleware+ref
  ([config middleware-key]
   (middleware+ref config middleware-key {} []))
  ([config middleware-key middleware-opts]
   (middleware+ref config middleware-key middleware-opts []))
  ([config middleware-key middleware-opts handlers]
   (-> config
       (middleware middleware-key middleware-opts)
       (ref-middleware middleware-key middleware-opts handlers))))

(defn keyboard-down+ref
  ([config keyboard-key keyboard-down-key]
   (keyboard-down+ref config keyboard-key keyboard-down-key {}))
  ([config keyboard-key keyboard-down-key keyboard-opts]
   (-> config
       (keyboard keyboard-key keyboard-opts)
       (ref-keyboard-down keyboard-key keyboard-down-key))))

(defn keyboard-up+ref
  ([config keyboard-key keyboard-up-key]
   (keyboard-up+ref config keyboard-key keyboard-up-key {}))
  ([config keyboard-key keyboard-up-key keyboard-opts]
   (-> config
       (keyboard keyboard-key keyboard-opts)
       (ref-keyboard-up keyboard-key keyboard-up-key))))

(defn keyboard-while-down+ref
  ([config keyboard-key keyboard-while-down-key]
   (keyboard-while-down+ref config keyboard-key keyboard-while-down-key {}))
  ([config keyboard-key keyboard-while-down-key keyboard-opts]
   (-> config
       (keyboard keyboard-key keyboard-opts)
       (ref-keyboard-while-down keyboard-key keyboard-while-down-key))))

(defn del-entity [config entity-key]
  (when-not (:scene/last (meta config)) (throw "Can only delete entities from scenes."))
  (-> config
      (update-in (conj (:scene/last (meta config)) :scene/ref :scene/entities) dissoc entity-key)
      (update-in (conj (:scene/last (meta config)) :scene/entities) dissoc entity-key)))

(defn del-component [config component-key]
  (when-not (:entity/last (meta config)) (throw "Can only delete components from entities."))
  (-> config
      (update-in (conj (:entity/last (meta config)) :entity/ref :entity/components) dissoc component-key)
      (update-in (conj (:entity/last (meta config)) :entity/components) dissoc component-key)))

(defn del-handler [config handler-key]
  (when-not (:component/last (meta config)) (throw "Can only delete handlers from components."))
  (-> config
      (update-in (conj (:component/last (meta config)) :component/ref :component/handlers) dissoc handler-key)
      (update-in (conj (:component/last (meta config)) :component/handlers) dissoc handler-key)))

(defn del-reactor [config reactor-key]
  (when-not (:component/last (meta config)) (throw "Can only delete reactors from components."))
  (-> config
      (update-in (conj (:component/last (meta config)) :component/ref :component/reactors) dissoc reactor-key)
      (update-in (conj (:component/last (meta config)) :component/reactors) dissoc reactor-key)))

(defn del-ticker [config ticker-key]
  (when-not (:component/last (meta config)) (throw "Can only delete tickers from components."))
  (-> config
      (update-in (conj (:component/last (meta config)) :component/ref :component/tickers) dissoc ticker-key)
      (update-in (conj (:component/last (meta config)) :component/tickers) dissoc ticker-key)))

(defn del-middleware [config middleware-key]
  (when-not (:component/last (meta config)) (throw "Can only delete middlewares from components."))
  (-> config
      (update-in (conj (:component/last (meta config)) :component/ref :component/middlewares) dissoc middleware-key)
      (update-in (conj (:component/last (meta config)) :component/middlewares) dissoc middleware-key)))

(defn initial-scene
  ([config]
   (if-let [[scene-key] (get (meta config) :scene/last)]
     (initial-scene config (top-key scene-key))
     (throw (ex-info "You can only mark scenes as intial-scene"
                     {:reason ::invalid-scene-key}))))
  ([config scene]
   (assoc config :rooij/initial-scene scene)))

(defn persistent
  "Make a component persistent. When a persistent component is initialized they
  will receive an extra key; `context/state`. This holds the persisted state of
  the component. If no state has been persisted yet (first initialization) then
  this key will be nil"
  [config]
  (when-not (:component/last (meta config)) (throw "Can only make components persistent."))
  (update-in config (:component/last (meta config)) assoc :component/persistent true))

(defn auto-persistent
  "Make a component auto-persistent. If an auto-persistent component has any
  persisted state (if it has already been initiated before) then the component
  will not be initiated, and instead return the persisted state immediately."
  [config]
  (when-not (:component/last (meta config)) (throw "Can only make components persistent."))
  (update-in config (:component/last (meta config)) assoc :component/auto-persistent true))

(defn dynamic
  [config]
  (when-not (:entity/last (meta config)) (throw "Can only make entities dynamic."))
  (update-in config (:entity/last (meta config)) assoc :entity/dynamic true))

(defn middleware-handlers
  [config handlers]
  (when-not (:middleware/last (meta config)) (throw "Can only add handlers to middleware."))
  (update-in config (conj (:middleware/last (meta config)) :middleware/handlers) concat handlers))

(defn save! [config]
  (rooij.config/merge-user! config)
  config)

(defn save-interface! [config]
  (rooij.config/merge-interface! config)
  config)

(defn select-scene
  [config scene-key]
  (vary-meta config assoc :scene/last [scene-key]))

(defn select-entity
  [config entity-key]
  (vary-meta config
             assoc :entity/last
             (conj (:scene/last (meta config))
                   :scene/entities
                   entity-key)))

(defn select-component
  [config component-key]
  (vary-meta config
             assoc :component/last
             (conj (:entity/last (meta config))
                   :entity/components
                   component-key)))

(defn select-handler
  [config handler-key]
  (vary-meta config
             assoc :handler/last
             (conj (:component/last (meta config))
                   :component/handlers
                   handler-key)))

(defn select-reactor
  [config reactor-key]
  (vary-meta config
             assoc :reactor/last
             (conj (:component/last (meta config))
                   :component/reactors
                   reactor-key)))

(defn select-ticker
  [config ticker-key]
  (vary-meta config
             assoc :ticker/last
             (conj (:component/last (meta config))
                   :component/tickers
                   ticker-key)))

(defn select-middleware
  [config middleware-key]
  (vary-meta config
             assoc :middleware/last
             (conj (:component/last (meta config))
                   :component/middlewares
                   middleware-key)))

(defn select-from-context
  ([context]
   (select-from-context {} context))
  ([config {:context/keys [scene-key entity-key component-key]}]
   (cond-> config
     scene-key (select-scene scene-key)
     entity-key (select-entity entity-key)
     component-key (select-component component-key))))

(defn select
  ([config scene-key]
   (select-scene config scene-key))
  ([config scene-key entity-key]
   (-> config
       (select-scene scene-key)
       (select-entity entity-key)))
  ([config scene-key entity-key component-key]
   (-> config
       (select-scene scene-key)
       (select-entity entity-key)
       (select-component component-key))))
