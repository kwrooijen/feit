 (ns rooij.dsl
  (:require
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]
   [rooij.util :refer [top-key]]
   [rooij.config]))

(defn new-child-key [k]
  (if (vector? k)
    k
    (make-child k)))

(def current-key
  (comp :current-key
        meta))

(defn scene
  ([scene-key] (scene  {} scene-key))
  ([config scene-key]
   (with-meta
     (meta-merge config {[:rooij/scene scene-key] {}})
     {:current-key [:rooij/scene scene-key]})))

(defn entity
  ([entity-key] (entity  {} entity-key))
  ([config entity-key]
   (with-meta
     (meta-merge config {[:rooij/entity entity-key] {}})
     {:current-key [:rooij/entity entity-key]})))

(defn add-entity
  ([config entity-key]
   (add-entity config entity-key {}))
  ([config entity-key entity-config]
   (let [scene-key (current-key config)
         entity
         ;; (merge entity-config {:entity/ref (ig/ref entity-key)})
         ;; TODO
         (ig/ref entity-key)]
     (when-not (#{:rooij/scene} (first scene-key))
       (throw (ex-info "You can only add entities to scenes" {:reason ::invalid-config})))
     (meta-merge config
                 {scene-key {:scene/entities [entity]}}
                 {[:rooij/entity entity-key] {}}))))

(defn add-component
  ([config component-key]
   (add-component config component-key {}))
  ([config component-key component-config]
   (let [entity-key (current-key config)
         component-key (new-child-key component-key)
         component (merge component-config {:component/ref (ig/ref (top-key component-key))})]
     (when-not (#{:rooij/entity} (first entity-key))
       (throw (ex-info "You can only add components to entities" {:reason ::invalid-config})))
     (meta-merge config
                 {entity-key {:entity/components [component]}}
                 {component-key component-config}))))

(defn initial-scene
  ([config]
   (let [[key-type scene-key] (current-key config)]
     (when-not (#{:rooij/scene} key-type)
       (throw (ex-info "You can only mark scenes as intial-scene"
                       {:reason ::invalid-scene-key})))
     (initial-scene config scene-key)))
  ([config scene]
   (assoc config :rooij/initial-scene scene)))

(defn save! [config]
  (rooij.config/merge-user! config))
