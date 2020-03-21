(ns essen.system.entity
  (:require
   [clojure.set]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.system :as es]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [essen.util :refer [vec->map top-key]]
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]))

(defn path-state
  [entity component]
  [:scene/entities entity
   :entity/components (:component/key component)
   :component/state])

(defn state [{:entity/keys [components]}]
  (transform [MAP-VALS] :component/state components))

(defn- handlers [{:entity/keys [components]}]
  (apply merge
         (for [{:component/keys [key handlers]} components
               k (keys handlers)]
           {k key})))

(defmethod es/init-key :essen/entity [k opts]
  (-> opts
      (update :entity/components vec->map :component/key)
      (assoc :entity/handlers (handlers opts)
             :entity/key (top-key k))))

(defn start [config key]
  (let [v (it/find-derived-value config key)
        components-replace (into {} (remove (comp (partial some nil?) flatten)
                                               (mapv (juxt (comp (partial it/find-derived-key config) :key)
                                                            (comp (partial it/find-derived-key v) :key)) (:entity/components v))))
        k (it/find-derived-key config key)
        v ((ig/init-key k v) v)
        dynamic? (:entity/dynamic v)
        key (if dynamic? (it.keyword/make-child key) key)
        config (clojure.set/rename-keys config components-replace)
        config (meta-merge config (dissoc v
                                          :entity/dynamic
                                          :entity/components
                                          :entity/subs
                                          :scene/opts
                                          :context/entity
                                          :context/scene))
        config (if dynamic?
                 (clojure.set/rename-keys config {k key})
                 config)
        system
        (try (es/init config [key])
             (catch #?(:clj Throwable :cljs :default) t
               (println "Failed to init entity" key
                        "because of key" (:key (ex-data t)))
               (println "Reason: " (:reason (ex-data t)))))]
    (with-meta
      (it/find-derived-value system key)
      {:system system})))

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
