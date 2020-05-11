(ns rooij.api
  (:require
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [make-child]]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [rooij.state :as state]
   [rooij.system.entity :as entity]
   [rooij.util :refer [->context top-key]]))

(defn scenes
  "Get all current running scenes as a set."
  []
  (-> (state/get-scenes)
      (keys)
      (set)))

(defn emit!
  "Emit a event with `content` to an `entity`'s `handler` in `scene`"
  ([context handler-key]
   (emit! context handler-key nil []))
  ([context handler-key content]
   (emit! context handler-key content []))
  ([{:context/keys [scene-key entity-key] :as context} handler-key content excludes]
   (swap! (state/get-scene-events scene-key) conj
          {:event/entity entity-key
           :event/handler handler-key
           :event/content content
           :event/excludes excludes})
   context))

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

(defn select
  ([scene-key entity-key]
   (select scene-key entity-key []))
  ([scene-key entity-key filters]
   (get (query scene-key entity-key filters) entity-key)))

(defn select-value
  ([scene-key entity-key]
   (select-value scene-key entity-key []))
  ([scene-key entity-key filters]
   (-> (select scene-key entity-key filters)
       (vals)
       (first))))

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

(defn add-entity!
  [config]
  (let [entity-key (-> config meta :entity/last last)
        scene-key (-> config meta :scene/last last top-key)
        entity-ref (it/find-derived-value @state/system entity-key)
        entity-key (if (:entity/dynamic entity-ref)
                     (make-child (top-key entity-key))
                     (top-key entity-key))
        entity (-> {:entity/ref entity-ref}
                   (meta-merge (get-in config (:entity/last (meta config))))
                   (->> (entity/preprocess-entity (->context scene-key entity-key) entity-key)
                        (entity/postprocess-entity)))]
    (swap! (state/get-scene-post-events scene-key) conj
           {:add/path [:scene/entities]
            :add/key entity-key
            :add/system entity
            :event/type :add/system})
    {:context/scene-key scene-key
     :context/entity-key entity-key}))
