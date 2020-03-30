(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.state :as state]
   [essen.util :as util]
   [essen.system :as system]
   [essen.system.component]
   [essen.system.handler]
   [essen.system.keyboard]
   [essen.system.middleware]
   [essen.system.reactor]
   [essen.system.scene :as scene]
   [essen.system.ticker]
   [essen.system.entity]
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [essen.render]
   [spec-signature.core :refer-macros [sdef]]
   [essen.module.pixi.render :as rr]))

(defn- start-render [config]
  (-> config
      (ig/prep [:essen.module/render])
      (ig/init [:essen.module/render])))

(defn- start-physics [config]
  (-> config
      (ig/prep [:essen.module/physics])
      (ig/init [:essen.module/physics])
      (it/find-derived-value :essen.module/physics)
      (rr/add-ticker :essen/physics)))

(defn setup
  [config]
  (system/start config)
  (start-render config)
  (start-physics config))

(defn scenes
  "Get all current running scenes as a set."
  []
  (-> (:essen/scenes @state/state)
      (keys)
      (set)))

(def ^:private event-keys
  [:event/entity
   :event/handler
   :event/content
   :event/excludes])

(defn emit!
  "Emit a event with `content` to an `entity`'s `handler` in `scene`"
  [{:event/keys [scene] :as event}]
  (swap! (get @state/events scene) conj (select-keys event event-keys)))

(defn entities
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  [scene-key entity-key]
  (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
       (into {})
       (transform [MAP-VALS] :entity/components)
       (transform [MAP-VALS MAP-VALS] :component/state)))

(defn entity [scene-key entity-key]
  (-> (entities scene-key entity-key)
      (get entity-key)
      (vals)
      (->> (apply merge))))

(defn transition-scene
  "Transition from `scene-from` to `scene-to`. Halts `scene-from` before
  initializing `scene-to`."
  [scene-from scene-to]
  (scene/halt! scene-from)
  (scene/start! scene-to))
