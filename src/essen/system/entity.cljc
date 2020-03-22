(ns essen.system.entity
  (:require
   [clojure.set]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.system :as system]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [essen.util :refer [vec->map top-key]]
   [meta-merge.core :refer [meta-merge]]
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
      (assoc :entity/handlers (handlers opts)
             :entity/key (top-key k))))

(defn- ref->derived-key [config]
  (comp (partial it/find-derived-key config)
        :key))

(defn- override-component-names
  "Renames any keys in `config` to the same (or derived) keys in `entity-opts`.
  When we merge `entity-opts` into `config`, duplicate keys will be overwritten.
  This prevents ambiguous keys in the final result, and all component opts will
  get properly merged."
  [config entity-opts]
  (->> (:entity/components entity-opts)
       (mapv (juxt (ref->derived-key config)
                   (ref->derived-key entity-opts)))
       (remove (comp (partial some nil?) flatten))
       (into {})
       (clojure.set/rename-keys config)))

(defn- try-init [config entity-key]
        (try (system/init config [entity-key])
             (catch #?(:clj Throwable :cljs :default) t
               (println "Failed to init entity" entity-key
                        "because of key" (:key (ex-data t)))
               (println "Reason: " (:reason (ex-data t))))))

(defn- rename-config-keys
  "Dynamic entities generate a new key and derives from the original. The
  original key nees to be overwritten during setup."
  [config derived-k entity-key dynamic?]
  (if dynamic?
    (clojure.set/rename-keys config {derived-k entity-key})
    config))

(defn- extract-system-with-meta
  "Extract the value of the built entity, and add the system as meta data. This
  metadata is later used for other integrant tasks."
  [system entity-key]
  (with-meta
    (it/find-derived-value system entity-key)
    {:system system}))

(defn- system->post-init-entity [system]
  (-> system
      (select-keys [:context/scene :context/entity
                    :entity/components :scene/opts
                    :entity/key])
      (->> (transform [:entity/components MAP-VALS] :component/state))))

(defn post-init [system]
  (-> system
      (system->post-init-entity)
      (system/post-init-key!))
  system)

(defn maybe-init-key [derived-k entity-opts]
  (if-let [f (get-method ig/init-key (ig/normalize-key derived-k))]
    ((f derived-k entity-opts) entity-opts)
    entity-opts))

(defn start [config key]
  (let [entity-opts (it/find-derived-value config key)
        derived-k (it/find-derived-key config key)
        entity-opts (maybe-init-key derived-k entity-opts)
        dynamic? (:entity/dynamic entity-opts)
        entity-key (if dynamic? (it.keyword/make-child key) key)]
    (-> config
        (override-component-names entity-opts)
        (meta-merge (apply dissoc entity-opts dissocables))
        (rename-config-keys derived-k entity-key dynamic?)
        (try-init entity-key)
        (extract-system-with-meta entity-key)
        (post-init))))

(defn halt! [entity]
  ;; TODO remove dynamic entity
  (ig/halt! (:system (meta entity))))

(defn suspend! [entity]
  (try (ig/suspend! (:system (meta entity)))
       (catch #?(:clj Throwable :cljs :default) t
         (println "Failed to halt entity" (:entity/key entity)
                  "because of key" (:key (ex-data t)))
         (println "Reason: " (:reason (ex-data t))))))

;; TODO Add a way to have some sort of "entity creator". You don't want to use
;; ig/init-key everytime you spawn a bullet.
