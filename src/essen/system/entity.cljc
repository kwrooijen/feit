(ns essen.system.entity
  (:require
   [clojure.set]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.system :as system]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [essen.util :refer [vec->map top-key spy]]
   [meta-merge.core :refer [meta-merge]]
   [essen.state :as state]
   [integrant.core :as ig]))

(def ^:private dissocables
  [:entity/dynamic
   :entity/components
   :entity/subs
   :scene/opts
   :context/entity
   :context/scene])

(defn path-state
  [entity component]
  [:scene/entities entity
   :entity/components (:component/key component)
   :component/state])

(defn state [{:entity/keys [components]}]
  (transform [MAP-VALS] :component/state components))

(defn components->nested-handlers [components]
  (for [{:component/keys [key handlers]} components
        k (keys handlers)]
    {k key}))

(defn- handlers [{:entity/keys [components]}]
  (apply merge (components->nested-handlers components)))

(defmethod system/init-key :essen/entity [k opts]
  (-> opts
      (update :entity/components vec->map :component/key)
      ;; TODO Rename handlers back to routes
      (select-keys [:entity/components])
      (assoc :entity/handlers (handlers opts)
             :entity/key (top-key k)
             :entity/init (system/get-init-key k opts)
             :entity/halt! (system/get-halt-key k opts))))

(defn- ref->derived-key [config]
  (comp (partial it/find-derived-key config)
        :key))

(defn- override-component-names
  "Renames any keys in `config` to the same (or derived) keys in `entity-opts`.
  When we merge `entity-opts` into `config`, duplicate keys will be overwritten.
  This prevents ambiguous keys in the final result, and all component opts will
  get properly merged."
  [config entity-opts]
  ;; TODO intersect between (it/children :essen/component) and component-key
  (->> (:entity/components entity-opts)
       (mapv (juxt (ref->derived-key config)
                   (ref->derived-key entity-opts)))
       ;; TODO Should throw an error if any nils are found
       (remove (comp (partial some nil?) flatten))
       (into {})
       ;; spy
       (clojure.set/rename-keys config)))

(defn- try-init [config entity-key]
        (try (system/init config [entity-key])
             (catch #?(:clj Throwable :cljs :default) t
               (println ;; Log error
                (str "Failed to init entity" entity-key "\n\n"
                     (when-let [key (:key (ex-data t))]
                       (str "Because of key: " key "\n\n"))
                     (when-let [value (:value (ex-data t))]
                       (str "Opts: " value "\n\n"))
                     (when-let [refs (:missing-refs (ex-data t))]
                       (str "Missing Refs:" refs "\n\n"))
                     (when-let [reason (:reason (ex-data t))]
                       (str "Reason:" reason "\n\n")))))))

(defn- extract-system-with-meta
  "Extract the value of the built entity, and add the system as meta data. This
  metadata is later used for other integrant tasks."
  [system entity-key]
  (with-meta
    (it/find-derived-value system entity-key)
    {:system system}))

(defn setup! [config entity-key]
  (let [entity-opts (it/find-derived-value config entity-key)]
    (-> config
        (override-component-names entity-opts)
        (meta-merge (apply dissoc entity-opts dissocables))
        (try-init entity-key)
        (extract-system-with-meta entity-key)
        (->> (swap! state/entities assoc entity-key)))))

(defn prep []
  (let [entities (it/find-derived-keys @state/config :essen/entity)]
    (doseq [entity entities :let [k (top-key entity)]]
      (setup! @state/config k))))

(defn halt! [{:entity/keys [components] :as entity}]
  ;; TODO remove dynamic entity
  (doseq [[_ component] components]
    ((:component/halt! component) component))
  ((:entity/halt! entity) entity))

(defn suspend! [entity]
  (doseq [[component-key component] (:entity/components entity)]
    (ig/suspend-key! component-key component))
  (ig/suspend-key! (:entity/key entity) entity))
