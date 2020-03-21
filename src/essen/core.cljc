(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
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
      (ig/prep [:matterjs/start])
      (ig/init [:matterjs/start])
      (it/find-derived-value :matterjs/start)
      (rr/add-ticker :essen/physics)))

(defn setup [{:keys [:essen/config :essen.module/render] :as game-config}]
  ((:essen/setup render) config)
  (reset! game (update game-config :essen/config system/prep))
  (start-physics config))

(defn emit!
  ([scene entity route content]
   (swap! (get @messages scene)
          conj {:message/entity entity
                :message/route route
                :message/content content})))

(defn scenes []
  (-> (:essen/scenes @state/state)
      (keys)
      (set)))

;; TODO Maybe use Specter to clean this logic up
(defn entities
  "Get all component states of any enitities from `scene-key` which are derived
  from `entity-key`"
  [scene-key entity-key]
  (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
       (map (fn [[k v]] [k (:entity/components v)]))
       (map (fn [[k v]] [k (into {} (map (fn [[kk vv]] [kk (:component/state vv)]) v))]))
       (into {})))

(defn entity [scene-key entity-key]
  (get (entities scene-key entity-key) entity-key))
