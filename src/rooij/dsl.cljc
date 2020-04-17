 (ns rooij.dsl
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child descendant?]]
   [rooij.util :refer [top-key]]
   [rooij.config]))

(def new-child-key
  (memoize
   (fn [_system-config k d]
     (cond
       (vector? k) k
       (descendant? k d) [k (make-child k)]
       :else [d k]))))

(def ^:private current-key
  (comp :current-key
        meta))

(def ^:private config-key
  (comp second
        current-key))

(def ^:private last-added-system
  (comp :last-added-system
        meta))

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
   (with-meta
     (meta-merge config {(->composite-key k system-key) system-opts})
     {:current-key [system-key (->composite-key k system-key)]})))

(defn- get-add-system-parent-system-key
  "Gets the parent key which the new system-key will be embedded in. The parent
  key is either a top level system (e.g. `(entity :foo/bar)`) or the last added
  entity (e.g. `(add-entity :foo/bar)`. Top level systems have priority."
  [config parent system-child-key]
  (cond (#{parent} (first (current-key config)))
        (current-key config)
        (get (last-added-system config) parent)
        (apply vec (last-added-system config))
        :else
        (throw (ex-info (str "You can only add " system-child-key " to " parent)
                        {:reason ::invalid-config}))))

(defn- add-system
  "Adds a system to `config` and reference that from the `parent-system-key`. "
  [config {:system/keys [system-child-key system-key system-config system-ref parent parent-collection]}]
  (let [parent-system-key (get-add-system-parent-system-key config parent system-child-key)
        system-child-key (new-child-key system-config system-child-key system-key)
        system-map {system-ref (ig/ref (top-key system-child-key))}]
    (vary-meta
     (meta-merge config
                 {(second parent-system-key) {parent-collection [system-map]}}
                 {system-child-key system-config})
     merge {:last-added-system {system-key system-child-key}})))

(defn- ref-system
  "Adds a reference to `system-child-key` to `parent-system-key`. Does not add
  the `system-child-key` to `config`. This is meant to reuse premade system. If
  you want to create a new system, use `add-system` instead."
  [config {:system/keys [system-child-key system-key system-config system-ref parent parent-collection]}]
  (let [parent-system-key (current-key config)
        system (merge system-config {system-ref (ig/ref (top-key system-child-key))})]
    (when-not (keyword? system-key)
      (throw (ex-info (str system-child-key "must be a keyword") {:reason ::invalid-ref-system-keyword})))
    (when-not (#{parent} (first parent-system-key))
      (throw (ex-info (str "You can only add " system-key " to " parent)
                      {:reason ::invalid-config})))
    (meta-merge config {(second parent-system-key) {parent-collection [system]}})))

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

(defn add-entity
  ([config entity-key]
   (add-entity config entity-key {}))
  ([config entity-key entity-config]
   (add-system config
               {:system/system-child-key entity-key
                :system/system-key :rooij/entity
                :system/system-config entity-config
                :system/system-ref :entity/ref
                :system/parent :rooij/scene
                :system/parent-collection :scene/entities})))

(defn add-component
  ([config component-key]
   (add-component config component-key {}))
  ([config component-key component-config]
   (add-system config
               {:system/system-child-key component-key
                :system/system-key :rooij/component
                :system/system-config component-config
                :system/system-ref :component/ref
                :system/parent :rooij/entity
                :system/parent-collection :entity/components})))

(defn add-handler
  ([config handler-key]
   (add-handler config handler-key {}))
  ([config handler-key handler-config]
   (add-system config
               {:system/system-child-key handler-key
                :system/system-key :rooij/handler
                :system/system-config handler-config
                :system/system-ref :handler/ref
                :system/parent :rooij/component
                :system/parent-collection :component/handlers})))

(defn add-ticker
  ([config ticker-key]
   (add-ticker config ticker-key {}))
  ([config ticker-key ticker-config]
   (add-system config
               {:system/system-child-key ticker-key
                :system/system-key :rooij/ticker
                :system/system-config ticker-config
                :system/system-ref :ticker/ref
                :system/parent :rooij/component
                :system/parent-collection :component/tickers})))

(defn add-reactor
  ([config reactor-key]
   (add-reactor config reactor-key {}))
  ([config reactor-key reactor-config]
   (add-system config
               {:system/system-child-key reactor-key
                :system/system-key :rooij/reactor
                :system/system-config reactor-config
                :system/system-ref :reactor/ref
                :system/parent :rooij/component
                :system/parent-collection :component/reactors})))

(defn add-middleware
  ([config middleware-key]
   (add-middleware config middleware-key {}))
  ([config middleware-key middleware-config]
   (add-system config
               {:system/system-child-key middleware-key
                :system/system-key :rooij/middleware
                :system/system-config middleware-config
                :system/system-ref :middleware/ref
                :system/parent :rooij/handler
                :system/parent-collection :handler/middlewares})))

(defn add-keydown
  ([config keyboard-key keyboard-down-key]
   (add-keydown config keyboard-key keyboard-down-key {}))
  ([config keyboard-key keyboard-down-key subs]
   (add-system config
               {:system/system-child-key keyboard-key
                :system/system-key :rooij/keyboard
                :system/system-config {:keyboard-down/key keyboard-down-key
                                       :keyboard/subs subs}
                :system/system-ref :keyboard/ref
                :system/parent :rooij/scene
                :system/parent-collection :scene/keyboard})))

(defn add-keyup
  ([config keyboard-key keyboard-up-key]
   (add-keyup config keyboard-key keyboard-up-key {}))
  ([config keyboard-key keyboard-up-key subs]
   (add-system config
               {:system/system-child-key keyboard-key
                :system/system-key :rooij/keyboard
                :system/system-config {:keyboard-up/key keyboard-up-key
                                       :keyboard/subs subs}
                :system/system-ref :keyboard/ref
                :system/parent :rooij/scene
                :system/parent-collection :scene/keyboard})))

(defn add-while-keydown
  ([config keyboard-key keyboard-down-key]
   (add-while-keydown config keyboard-key keyboard-down-key {}))
  ([config keyboard-key keyboard-down-key subs]
   (add-system config
               {:system/system-child-key keyboard-key
                :system/system-key :rooij/keyboard
                :system/system-config {:keyboard-while-down/key keyboard-down-key
                                       :keyboard/subs subs}
                :system/system-ref :keyboard/ref
                :system/parent :rooij/scene
                :system/parent-collection :scene/keyboard})))

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
  ([config middleware-key]
   (ref-middleware config middleware-key {}))
  ([config middleware-key middleware-config]
   (ref-system config
               {:system/system-child-key middleware-key
                :system/system-key :rooij/middleware
                :system/system-config middleware-config
                :system/system-ref :middleware/ref
                :system/parent :rooij/handler
                :system/parent-collection :handler/middlewares})))

(defn initial-scene
  ([config]
   (let [[key-type [_ scene-key]] (current-key config)]
     (when-not (#{:rooij/scene} key-type)
       (throw (ex-info "You can only mark scenes as intial-scene"
                       {:reason ::invalid-scene-key})))
     (initial-scene config scene-key)))
  ([config scene]
   (assoc config :rooij/initial-scene scene)))

(defn persistent
  "Make a component persistent. When a persistent component is initialized they
  will receive an extra key; `context/state`. This holds the persisted state of
  the component. If no state has been persisted yet (first initialization) then
  this key will be nil"
  [config]
  (when-not (#{:rooij/component} (first (current-key config)))
    (throw (ex-info "You can only make components persistent"
                    {:reason ::invalid-persistent-key})))
  (update config (config-key config) assoc :component/persistent true))

(defn auto-persistent
  "Make a component auto-persistent. If an auto-persistent component has any
  persisted state (if it has already been initiated before) then the component
  will not be initiated, and instead return the persisted state immediately."
  [config]
  (when-not (#{:rooij/component} (first (current-key config)))
    (throw (ex-info "You can only make components auto-persistent"
                    {:reason ::invalid-auto-persistent-key})))
  (update config (config-key config) assoc :component/auto-persistent true))

(defn position-emitter [config]
  (if-let [last-added-component (:rooij/component (last-added-system config))]
    (assoc-in config [last-added-component :component.position/emitter] true)
    (throw (ex-info (str "You can only make components position-emitters: " (last-added-system config))
                    {:reason ::invalid-position-emitter}))))

(defn save! [config]
  (rooij.config/merge-user! config))
