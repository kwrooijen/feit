(ns rooij.dsl
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]
   [rooij.util :refer [bottom-key top-key]]
   [rooij.config]))

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
  ([config scene-key]
   (scene config scene-key {}))
  ([config scene-key scene-opts]
   {:pre [(qualified-keyword? scene-key)]}
   (-> config
       (meta-merge {[:rooij/scene scene-key] scene-opts})
       (vary-meta assoc :scene/last [[:rooij/scene scene-key]]))))

(defn entity
  ([entity-key]
   (entity {} entity-key {}))
  ([config entity-key]
   (entity config entity-key {}))
  ([config entity-key entity-opts]
   {:pre [(qualified-keyword? entity-key)]}
   (-> config
       (meta-merge {[:rooij/entity entity-key] entity-opts})
       (vary-meta assoc :entity/last [[:rooij/entity entity-key]]))))

(defn component
  ([component-key]
   (component {} component-key {}))
  ([config component-key]
   (component config component-key {}))
  ([config component-key component-opts]
   {:pre [(qualified-keyword? component-key)]}
   (-> config
       (meta-merge {[:rooij/component component-key] component-opts})
       (vary-meta assoc :component/last [[:rooij/component component-key]]))))

(defn handler
  ([handler-key]
   (handler {} handler-key {}))
  ([config handler-key]
   (handler config handler-key {}))
  ([config handler-key handler-opts]
   {:pre [(qualified-keyword? handler-key)]}
   (-> config
       (meta-merge {[:rooij/handler handler-key] handler-opts})
       (vary-meta assoc :handler/last [[:rooij/handler handler-key]]))))

(defn ticker
  ([ticker-key]
   (ticker {} ticker-key {}))
  ([config ticker-key]
   (ticker config ticker-key {}))
  ([config ticker-key ticker-opts]
   {:pre [(qualified-keyword? ticker-key)]}
   (-> config
       (meta-merge {[:rooij/ticker ticker-key] ticker-opts})
       (vary-meta assoc :ticker/last [[:rooij/ticker ticker-key]]))))

(defn reactor
  ([reactor-key]
   (reactor {} reactor-key {}))
  ([config reactor-key]
   (reactor config reactor-key {}))
  ([config reactor-key reactor-opts]
   {:pre [(qualified-keyword? reactor-key)]}
   (-> config
       (meta-merge {[:rooij/reactor reactor-key] reactor-opts})
       (vary-meta assoc :reactor/last [[:rooij/reactor reactor-key]]))))

(defn middleware
  ([middleware-key]
   (middleware {} middleware-key {}))
  ([config middleware-key]
   (middleware config middleware-key {}))
  ([config middleware-key middleware-opts]
   {:pre [(qualified-keyword? middleware-key)]}
   (-> config
       (meta-merge {[:rooij/middleware middleware-key] middleware-opts})
       (vary-meta assoc :middleware/last [[:rooij/middleware middleware-key]]))))

(defn ref-entity
  ([config entity-key]
   (ref-entity config entity-key {}))
  ([config entity-key entity-config]
   (let [parent-path (conj (:scene/last (meta config)) :scene/entities)
         entity-id (if (:dynamic entity-config)
                     (make-child (top-key entity-key))
                     (top-key entity-key))
         full-path (conj parent-path entity-id)
         entity-config (-> entity-config
                           (assoc :entity/ref (ig/ref (bottom-key entity-key)))
                           (add-hierarchy entity-key))]
     (-> config
         (update-in full-path meta-merge entity-config)
         (vary-meta assoc :entity/last full-path)))))

(defn ref-component
  ([config component-key]
   (ref-component config component-key {}))
  ([config component-key component-config]
   (when-not (:entity/last (meta config)) (throw "Can only add components to entities."))
   (ref-system config component-key component-config :entity :components :component)))

(defn ref-handler
  ([config handler-key]
   (ref-handler config handler-key {}))
  ([config handler-key handler-config]
   (when-not (:component/last (meta config)) (throw "Can only add handlers to components."))
   (ref-system config handler-key handler-config :component :handlers :handler)))

(defn ref-ticker
  ([config ticker-key]
   (ref-ticker config ticker-key {}))
  ([config ticker-key ticker-config]
   (when-not (:component/last (meta config)) (throw "Can only add tickers to components."))
   (ref-system config ticker-key ticker-config :component :tickers :ticker)))

(defn ref-reactor
  ([config reactor-key]
   (ref-reactor config reactor-key {}))
  ([config reactor-key reactor-config]
   (when-not (:component/last (meta config)) (throw "Can only add reactors to components."))
   (ref-system config reactor-key reactor-config :component :reactors :reactor)))

(defn ref-middleware
  ([config middleware-key]
   (ref-middleware config middleware-key {} []))
  ([config middleware-key middleware-config]
   (ref-middleware config middleware-key middleware-config []))
  ([config middleware-key middleware-config handlers]
   (when-not (:component/last (meta config)) (throw "Can only add middlewares to components."))
   (ref-system config
               middleware-key
               (assoc middleware-config :middleware/handlers handlers)
               :component :middlewares :middleware)))

(defn entity+ref
  ([config entity-key]
   (entity+ref config entity-key {}))
  ([config entity-key entity-config]
   (-> config
       (entity entity-key entity-config)
       (ref-entity entity-key))))

(defn component+ref
  ([config component-key]
   (component+ref config component-key {}))
  ([config component-key component-config]
   (-> config
       (component component-key component-config)
       (ref-component component-key))))

(defn handler+ref
  ([config handler-key]
   (handler+ref config handler-key {}))
  ([config handler-key handler-config]
   (-> config
       (handler handler-key handler-config)
       (ref-handler handler-key))))

(defn ticker+ref
  ([config ticker-key]
   (ticker+ref config ticker-key {}))
  ([config ticker-key ticker-config]
   (-> config
       (ticker ticker-key ticker-config)
       (ref-ticker ticker-key))))

(defn reactor+ref
  ([config reactor-key]
   (reactor+ref config reactor-key {}))
  ([config reactor-key reactor-config]
   (-> config
       (reactor reactor-key reactor-config)
       (ref-reactor reactor-key))))

(defn middleware+ref
  ([config middleware-key]
   (middleware+ref config middleware-key {}))
  ([config middleware-key middleware-config]
   (-> config
       (middleware middleware-key middleware-config)
       (ref-middleware middleware-key))))

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

(defn is-dynamic
  [config]
  (when-not (:entity/last (meta config)) (throw "Can only make entities dynamic."))
  (update-in config (:component/last (meta config)) assoc :entity/dynamic true))

(defn middleware-handlers
  [config handlers]
  (when-not (:middleware/last (meta config)) (throw "Can only add handlers to middleware."))
  (update-in config (conj (:middleware/last (meta config)) :middleware/handlers) concat handlers))


;; (defn position-emitter [config]
;;   (if-let [last-added-component (:rooij/component (last-added-system config))]
;;     (assoc-in config [last-added-component :component.position/emitter] true)
;;     (throw (ex-info (str "You can only make components position-emitters: " (last-added-system config))
;;                     {:reason ::invalid-position-emitter}))))

(defn save! [config]
  (rooij.config/merge-user! config)
  config)

(defn save-interface! [config]
  (rooij.config/merge-interface! config)
  config)
