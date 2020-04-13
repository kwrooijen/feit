(ns rooij.core
  (:require
   [rooij.error]
   [rooij.logger]
   [rooij.config]
   [taoensso.timbre :as timbre]
   [rooij.loop.core]
   [clojure.spec.alpha :as s]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [rooij.state :as state]
   [rooij.system.core :as system]
   [rooij.system.component]
   [rooij.system.handler]
   [rooij.system.keyboard]
   [rooij.system.middleware]
   [rooij.system.reactor]
   [rooij.system.scene :as scene]
   [rooij.system.ticker]
   [rooij.system.entity]
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [rooij.interface.graphics-2d.core :as interface.graphics-2d]
   [rooij.interface.physics-2d.core :as interface.physics-2d]))

(set! *print-meta* true)

(defn- start []
  (system/start)
  (interface.graphics-2d/init)
  (interface.physics-2d/init)
  (rooij.loop.core/start!)
  (scene/start-initial-scene))

(defn setup
  ([] (setup {}))
  ([config]
   (rooij.config/merge-user! config)
   (try
     (timbre/debug ::setup @rooij.config/config)
     (start)
     (catch #?(:clj Throwable :cljs :default) e
       (rooij.error/handle-error e)
       (start)))))

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
