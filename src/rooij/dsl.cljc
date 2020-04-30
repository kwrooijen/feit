(ns rooij.dsl
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]
   [integrant-tools.core :as it]
   [rooij.util :refer [bottom-key top-key]]
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
                                            :system/system-key system-key})
       (vary-meta assoc :last-system [(->composite-key k system-key)]))))

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

(defn ref-system [config system-key system-config last-parent parent-collection system-ref last-system]
   (let [parent-path (conj (last-parent (meta config)) parent-collection)
         component-id (top-key system-key)
         full-path (conj parent-path component-id)
         system-config (assoc system-config system-ref (ig/ref (bottom-key system-key)))]
     (-> config
         (update-in full-path meta-merge system-config)
         (vary-meta assoc last-system full-path))))

(defn scene [& args]
  (let [config (apply system (concat args [:rooij/scene]))]
    (vary-meta config assoc :last-scene (-> config meta :last-system))))

(defn entity [& args]
  (let [config (apply system (concat args [:rooij/entity]))]
    (vary-meta config assoc :last-entity (-> config meta :last-system))))

(defn component [& args]
  (let [config (apply system (concat args [:rooij/component]))]
    (vary-meta config assoc :last-component (-> config meta :last-system))))

(defn handler [& args]
  (let [config (apply system (concat args [:rooij/handler]))]
    (vary-meta config assoc :last-handler (-> config meta :last-system))))

(defn reactor [& args]
  (let [config (apply system (concat args [:rooij/reactor]))]
    (vary-meta config assoc :last-reactor (-> config meta :last-system))))

(defn ticker [& args]
  (let [config (apply system (concat args [:rooij/ticker]))]
    (vary-meta config assoc :last-ticker (-> config meta :last-system))))

(defn middleware [& args]
  (let [config (apply system (concat args [:rooij/middleware]))]
    (vary-meta config assoc :last-middleware (-> config meta :last-system))))

(defn ref-entity
  ([config entity-key]
   (ref-entity config entity-key {}))
  ([config entity-key entity-config]
   (let [parent-path (conj (:last-scene (meta config)) :scene/entities)
         entity-id (if (:dynamic entity-config)
                     (make-child (top-key entity-key))
                     (top-key entity-key))
         full-path (conj parent-path entity-id)
         entity-config (assoc entity-config :entity/ref (ig/ref (bottom-key entity-key)))]
     (-> config
         (update-in full-path meta-merge entity-config)
         (vary-meta assoc :last-entity full-path)))))

(defn ref-component
  ([config component-key]
   (ref-component config component-key {}))
  ([config component-key component-config]
   (when-not (:last-entity (meta config)) (throw "Can only add components to entities."))
   (ref-system config component-key component-config :last-entity :entity/components :component/ref :last-component)))

(defn ref-handler
  ([config handler-key]
   (ref-handler config handler-key {}))
  ([config handler-key handler-config]
   (when-not (:last-component (meta config)) (throw "Can only add handlers to components."))
   (ref-system config handler-key handler-config :last-component :component/handlers :handler/ref :last-handler)))

(defn ref-ticker
  ([config ticker-key]
   (ref-ticker config ticker-key {}))
  ([config ticker-key ticker-config]
   (when-not (:last-component (meta config)) (throw "Can only add tickers to components."))
   (ref-system config ticker-key ticker-config :last-component :component/tickers :ticker/ref :last-ticker)))

(defn ref-reactor
  ([config reactor-key]
   (ref-reactor config reactor-key {}))
  ([config reactor-key reactor-config]
   (when-not (:last-component (meta config)) (throw "Can only add reactors to components."))
   (ref-system config reactor-key reactor-config :last-component :component/reactors :reactor/ref :last-reactor)))

(defn ref-middleware
  ([config middleware-key handlers]
   (ref-middleware config middleware-key handlers {}))
  ([config middleware-key handlers middleware-config]
   (when-not (:last-component (meta config)) (throw "Can only add middlewares to components."))
   (ref-system config middleware-key
                 (assoc middleware-config :middleware/handlers handlers)
                 :last-component :component/middlewares :middleware/ref :last-middleware)))

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
