(ns rooij.api
  (:require
   [rooij.state :as state]
   [rooij.util :refer [top-key bottom-key]]
   [integrant-tools.core :as it]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]))

;; TODO Remove all the below from core
(defn scenes
  "Get all current running scenes as a set."
  []
  (-> (state/get-scenes)
      (keys)
      (set)))

(def ^:private event-keys
  [:event/entity
   :event/handler
   :event/content
   :event/excludes])

(defn emit!
  "Emit a event with `content` to an `entity`'s `handler` in `scene`"
  ([{:event/keys [scene] :as event}]
   (swap! (state/get-scene-events scene) conj (select-keys event event-keys)))
  ([context handler-key]
   (emit! context handler-key {}))
  ([{:context/keys [scene-key entity-key]} handler-key content]
   (swap! (state/get-scene-events scene-key) conj
          {:event/entity entity-key
           :event/handler handler-key
           :event/content content
           :event/excludes []}))
  ([scene-key entity-key handler-key content]
   (swap! (state/get-scene-events scene-key) conj
          {:event/entity entity-key
           :event/handler handler-key
           :event/content content
           :event/excludes []})))

(defn entities
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  [scene-key entity-key]
  (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
       (into {})
       (sp/transform [MAP-VALS] :entity/state)))

(defn- apply-query-filters [filters v]
  (filter (fn [[_ state]]
            (every?
             (fn [[fk fp]]
               (fp (get state fk)))
             filters))
          v))

(defn query
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  ([scene-key entity-key]
   (query scene-key entity-key []))
  ([scene-key entity-key filters]
   (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
        (map (fn [[k v]] [k (:entity/state v)]))
        (apply-query-filters filters)
        (into {}))))

(defn query-keys
  ([scene-key entity-key]
   (query-keys scene-key entity-key []))
  ([scene-key entity-key filters]
   (keys (query scene-key entity-key filters))))

(defn entity [scene-key entity-key]
  (-> (entities scene-key entity-key)
      (get entity-key)
      (vals)
      (->> (apply merge))))

(defn ref-entity [scene entity-key opts]
  ;; (it/derive-composite entity-key)
  (let [ref (it/find-derived-value @state/system (bottom-key entity-key))
        identifier (if (:dynamic opts)
                     (make-child (top-key entity-key))
                     (top-key entity-key))]
    (assoc-in scene [:scene/entities identifier]
              (merge {:entity/ref ref} opts))))

(defn transition-scene
  "Transition from `scene-from` to `scene-to`. Halts `scene-from` before
  initializing `scene-to`."
  [scene-from scene-to]
  (swap! (state/get-scene-post-events scene-from) conj
         {:scene/key scene-to
          :event/type :scene/start!})
  (swap! (state/get-scene-post-events scene-from) conj
         {:scene/key scene-from
          :event/type :scene/halt!}))

(defn restart-scene
  [scene-key]
  (swap! (state/get-scene-post-events scene-key) conj
         {:scene/key scene-key
          :event/type :scene/halt!})
  (swap! (state/get-scene-post-events scene-key) conj
         {:scene/key scene-key
          :event/type :scene/start!}))
