(ns rooij.core
  (:require
   [rooij.util :refer [top-key bottom-key]]
   [integrant-tools.core :as it]
   [com.rpl.specter :as sp :refer [MAP-VALS]]
   [integrant.core :as ig]
   [integrant-tools.keyword :refer [make-child]]
   [rooij.config]
   [rooij.error]
   [rooij.interface.graphics-2d.core :as interface.graphics-2d]
   [rooij.interface.physics-2d.core :as interface.physics-2d]
   [rooij.interface.keyboard.core :as interface.keyboard]
   [rooij.logger]
   [rooij.loop.core]
   [rooij.state :as state]
   [rooij.system.component]
   [rooij.system.core :as system]
   [rooij.system.entity]
   [rooij.system.handler]
   [rooij.system.keyboard]
   [rooij.system.middleware]
   [rooij.system.reactor]
   [rooij.system.scene :as scene]
   [rooij.system.ticker]
   [taoensso.timbre :as timbre]))

(set! *print-meta* true)

(defn- start []
  (system/start)
  (interface.graphics-2d/init)
  (interface.physics-2d/init)
  (interface.keyboard/init)
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
       (throw e)))))

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
           :event/excludes []})))

(defn entities
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  [scene-key entity-key]
  (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
       (into {})
       (sp/transform [MAP-VALS] :entity/state)))

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
