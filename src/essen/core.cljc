(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [essen.state :as state :refer [messages game]]
   [essen.system :as system]
   [essen.system.component]
   [essen.system.handler]
   [essen.system.keyboard]
   [essen.system.middleware]
   [essen.system.reactor]
   [essen.system.scene]
   [essen.system.ticker]
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [essen.render]
   [spec-signature.core :refer-macros [sdef]]
   [essen.module.pixi.render :as rr]))

(defn- start-physics [config]
  (-> config
      (ig/prep [:essen.physics/start])
      (ig/init [:essen.physics/start])
      (it/find-derived-value :essen.physics/start)
      (rr/add-ticker :essen/physics)))

(defn setup
  [{:keys [:essen/config :essen.module/render] :as game-config}]
  ((:essen/setup render) config)
  (reset! game (update game-config :essen/config system/prep))
  (start-physics config))

(defn emit!
  "Emit a message with `content` to an `entity`'s `handler` in `scene`"
  ([scene entity handler content]
   (swap! (get @messages scene)
          conj {:message/entity entity
                :message/handler handler
                :message/content content})))

(defn scenes
  "Get all current running scenes as a set."
  []
  (-> (:essen/scenes @state/state)
      (keys)
      (set)))

(defn entities
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  [scene-key entity-key]
  (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
       (into {})
       (transform [MAP-VALS] :entity/components)
       (transform [MAP-VALS MAP-VALS] :component/state)))

(defn entity [scene-key entity-key]
  (get (entities scene-key entity-key) entity-key))
