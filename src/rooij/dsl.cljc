(ns rooij.dsl
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]
   [rooij.util :refer [bottom-key top-key]]
   [rooij.config]))

(defn- ->composite-key [k ck]
  (if (vector? k)
    k
    [ck k]))

(defn- system
  ([k system-key] (system {} k {} system-key))
  ([component-key--config component-key--component-opts system-key]
   (if (or (keyword? component-key--config) (vector? component-key--config))
     (system {} component-key--config component-key--component-opts system-key)
     (system component-key--config component-key--component-opts {} system-key)))
  ([config k system-opts system-key]
   (let [system-key (->composite-key k system-key)]
     [system-key (meta-merge config {system-key system-opts})])))

(defn- ref-system [config system-key system-config parent collection child]
  (let [parent-path (conj ((keyword parent "last") (meta config)) (keyword parent collection))
        component-id (top-key system-key)
        full-path (conj parent-path component-id)
        system-config (assoc system-config (keyword child "ref") (ig/ref (bottom-key system-key)))]
    (-> config
        (update-in full-path meta-merge system-config)
        (vary-meta assoc (keyword child "last") full-path))))

(defn scene [& args]
  (let [[k config] (apply system (concat args [:rooij/scene]))]
    (vary-meta config assoc :scene/last [k])))

(defn entity [& args]
  (let [[k config] (apply system (concat args [:rooij/entity]))]
    (vary-meta config assoc :entity/last [k])))

(defn component [& args]
  (let [[k config] (apply system (concat args [:rooij/component]))]
    (vary-meta config assoc :component/last [k])))

(defn handler [& args]
  (let [[k config] (apply system (concat args [:rooij/handler]))]
    (vary-meta config assoc :handler/last [k])))

(defn reactor [& args]
  (let [[k config] (apply system (concat args [:rooij/reactor]))]
    (vary-meta config assoc :reactor/last [k])))

(defn ticker [& args]
  (let [[k config] (apply system (concat args [:rooij/ticker]))]
    (vary-meta config assoc :ticker/last [k])))

(defn middleware [& args]
  (let [[k config] (apply system (concat args [:rooij/middleware]))]
    (vary-meta config assoc :middleware/last [k])))

(defn ref-entity
  ([config entity-key]
   (ref-entity config entity-key {}))
  ([config entity-key entity-config]
   (let [parent-path (conj (:scene/last (meta config)) :scene/entities)
         entity-id (if (:dynamic entity-config)
                     (make-child (top-key entity-key))
                     (top-key entity-key))
         full-path (conj parent-path entity-id)
         entity-config (assoc entity-config :entity/ref (ig/ref (bottom-key entity-key)))]
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
  ([config middleware-key handlers]
   (ref-middleware config middleware-key handlers {}))
  ([config middleware-key handlers middleware-config]
   (when-not (:component/last (meta config)) (throw "Can only add middlewares to components."))
   (ref-system config middleware-key
                 (assoc middleware-config :middleware/handlers handlers)
                 :component :middlewares :middleware)))

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
