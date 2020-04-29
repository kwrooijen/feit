(ns rooij.dsl
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]
   [rooij.util :refer [top-key]]
   [rooij.config]))

;; (def ^:private current-key
;;   (comp :current-key
;;         meta))

;; (def ^:private config-key
;;   (comp second
;;         current-key))

;; (def ^:private last-added-system
;;   (comp :last-added-system
;;         meta))

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
   (-> config
       (meta-merge {(->composite-key k system-key) system-opts})
       (vary-meta assoc :last-added-system {:system/key (->composite-key k system-key)
                                            :system/system-key system-key}))))

(defn get-ref-target-key [config parent system-key]
  (let [system (get (meta config) :last-added-system)
        ref (get (meta config) :last-ref-system)]
    (cond
      (#{parent} (:system/system-key system)) system
      (#{parent} (:ref/parent-system-key ref)) ref
      :else (throw (ex-info (str "You can only add " system-key " to " parent)
                            {:reason ::invalid-config})))))

(defn get-system-child-identifier [{:system/keys [system-child-key system-config]}]
  (if (:dynamic system-config)
    (make-child system-child-key)
    system-child-key))

(defn- ref-system
  "Adds a reference to `system-child-key` to `parent-system-key`. Does not add
  the `system-child-key` to `config`. This is meant to reuse a premade system. If
  you want to create a new system, use `add-system` instead."
  [config {:system/keys [system-child-key system-key system-config system-ref parent parent-collection] :as system}]
  (let [parent-system-key (get-ref-target-key config parent system-key)
        system-child-identifier (get-system-child-identifier system)
        system (merge system-config
                      {system-ref (ig/ref (top-key system-child-key))})]
    (when-not (keyword? system-key)
      (throw (ex-info (str system-child-key "must be a keyword")
                      {:reason ::invalid-ref-system-keyword})))
    (if (:system/key parent-system-key)
      (-> config
          (meta-merge {(:system/key parent-system-key) {parent-collection {system-child-identifier system}}})
          (vary-meta assoc :last-ref-system {:ref/ref system-child-key
                                             :ref/parent-system-key system-key
                                             :ref/parent (:system/key parent-system-key)}))
      (do
        (println "TODO")
        config))))

(defn scene [& args]
  (apply system (concat args [:rooij/scene])))

(defn entity [& args]
  (apply system (concat args [:rooij/entity])))

(defn component [& args]
  (apply system (concat args [:rooij/component])))

(defn handler [& args]
  (apply system (concat args [:rooij/handler])))

(defn reactor [& args]
  (apply system (concat args [:rooij/reactor])))

(defn ticker [& args]
  (apply system (concat args [:rooij/ticker])))

(defn middleware [& args]
  (apply system (concat args [:rooij/middleware])))


(defn ref-entity
  ([config entity-key]
   (ref-entity config entity-key {}))
  ([config entity-key entity-config]
   (ref-system config
               {:system/system-child-key entity-key
                :system/system-key :rooij/entity
                :system/system-config entity-config
                :system/system-ref :entity/ref
                :system/parent :rooij/scene
                :system/parent-collection :scene/entities})))

(defn ref-component
  ([config component-key]
   (ref-component config component-key {}))
  ([config component-key component-config]
   (ref-system config
               {:system/system-child-key component-key
                :system/system-key :rooij/component
                :system/system-config component-config
                :system/system-ref :component/ref
                :system/parent :rooij/entity
                :system/parent-collection :entity/components})))

(defn ref-handler
  ([config handler-key]
   (ref-handler config handler-key {}))
  ([config handler-key handler-config]
   (ref-system config
               {:system/system-child-key handler-key
                :system/system-key :rooij/handler
                :system/system-config handler-config
                :system/system-ref :handler/ref
                :system/parent :rooij/component
                :system/parent-collection :component/handlers})))

(defn ref-ticker
  ([config ticker-key]
   (ref-ticker config ticker-key {}))
  ([config ticker-key ticker-config]
   (ref-system config
               {:system/system-child-key ticker-key
                :system/system-key :rooij/ticker
                :system/system-config ticker-config
                :system/system-ref :ticker/ref
                :system/parent :rooij/component
                :system/parent-collection :component/tickers})))

(defn ref-reactor
  ([config reactor-key]
   (ref-reactor config reactor-key {}))
  ([config reactor-key reactor-config]
   (ref-system config
               {:system/system-child-key reactor-key
                :system/system-key :rooij/reactor
                :system/system-config reactor-config
                :system/system-ref :reactor/ref
                :system/parent :rooij/component
                :system/parent-collection :component/reactors})))

(defn ref-middleware
  ([config middleware-key handlers]
   (ref-middleware config middleware-key handlers {}))
  ([config middleware-key handlers middleware-config]
   (ref-system config
               {:system/system-child-key middleware-key
                :system/system-key :rooij/middleware
                :system/system-config (assoc middleware-config
                                             :middleware/handlers handlers)
                :system/system-ref :middleware/ref
                :system/parent :rooij/component
                :system/parent-collection :component/middlewares})))

(defn initial-scene
  ([config]
   (let [{:system/keys [system-key key]} (get (meta config) :last-added-system)]
     (if (#{:rooij/scene} system-key)
         (initial-scene config (last key))
         (throw (ex-info "You can only mark scenes as intial-scene"
                         {:reason ::invalid-scene-key})))))
  ([config scene]
   (assoc config :rooij/initial-scene scene)))

;; (defn persistent
;;   "Make a component persistent. When a persistent component is initialized they
;;   will receive an extra key; `context/state`. This holds the persisted state of
;;   the component. If no state has been persisted yet (first initialization) then
;;   this key will be nil"
;;   [config]
;;   (when-not (#{:rooij/component} (first (current-key config)))
;;     (throw (ex-info "You can only make components persistent"
;;                     {:reason ::invalid-persistent-key})))
;;   (update config (config-key config) assoc :component/persistent true))

;; (defn auto-persistent
;;   "Make a component auto-persistent. If an auto-persistent component has any
;;   persisted state (if it has already been initiated before) then the component
;;   will not be initiated, and instead return the persisted state immediately."
;;   [config]
;;   (when-not (#{:rooij/component} (first (current-key config)))
;;     (throw (ex-info "You can only make components auto-persistent"
;;                     {:reason ::invalid-auto-persistent-key})))
;;   (update config (config-key config) assoc :component/auto-persistent true))

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
