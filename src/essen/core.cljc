(ns essen.core
  (:require
   [essen.loop.core]
   [clojure.spec.alpha :as s]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [essen.state :as state]
   [essen.system.core :as system]
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

   #?(:clj [clojure.stacktrace :as st]
      :cljs [cljs.stacktrace :as st])
   [essen.interface.graphics-2d.entity]
   [essen.interface.graphics-2d.core :as interface.graphics-2d]))

(set! *print-meta* true)

(defn- start-graphics-2d [config]
  (-> config
      (ig/prep [interface.graphics-2d/system])
      (ig/init [interface.graphics-2d/system])
      (it/find-derived-value interface.graphics-2d/system)
      (state/set-graphics-2d!))

  (state/set-graphics-2d-scene!
   {:init (ig/init-key (first (descendants interface.graphics-2d/scene)) {})
    :halt! (ig/halt-key! (first (descendants interface.graphics-2d/scene)){})}))


;; (defn- start-physics [config]
;;   (-> config
;;       (ig/prep [:essen.module/physics])
;;       (ig/init [:essen.module/physics])
;;       (it/find-derived-value :essen.module/physics)
;;       (essen.loop.core/add!)))

(defn- start [config]
  (system/start config)
  (start-graphics-2d config)
  ;; (start-physics config)
  (essen.loop.core/start!))

(defn setup
  [config]
  (try
    (start config)
    (catch #?(:clj Throwable :cljs :default) t
      ;; TODO Handle errors
      (start config))))

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
  [{:event/keys [scene] :as event}]
  (swap! (state/get-scene-events scene) conj (select-keys event event-keys)))

(defn entities
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  [scene-key entity-key]
  (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
       (into {})
       (sp/transform [MAP-VALS] :entity/components)
       (sp/transform [MAP-VALS MAP-VALS] :component/state)))

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
