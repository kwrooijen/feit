(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [essen.state :as state :refer [input-messages messages game systems]]
   [essen.util :refer [vec->map spy]]
   [essen.system]
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
   [spec-signature.core :refer-macros [sdef]]))

(defmethod ig/init-key :essen/const [_ opts] opts)

(defn- derive-components
  "Globally derive all components in `config` to make them available"
  [config]
  (doseq [k (it/find-derived-keys config :essen/component)]
    (it/derive-composite k)))

(defn add-scene-opts-ref
  "This scene ref is used to give a scene arguments. For example, if you go
  into a 'battle' scene, you can set a level, scaling all enemies in that level.
  Or you can set the amount of enemies in a scene."
  [acc k v]
  (assoc acc k
         (cond-> v
           (map? v) (assoc :scene/opts (ig/ref :scene/opts)
                           :entity/opts (ig/ref :entity/opts)))))

(defn prep [config]
  (->> config
       (reduce-kv add-scene-opts-ref {})
       (ig/prep)))

(defn setup [{:keys [:essen/config :essen.module/render] :as game-config}]
  ((:essen/setup render) config)
  (derive-components config)
  (reset! game (update game-config :essen/config prep)))

(defn emit!
  ([{:context/keys [scene entity]} route content]
   (emit! scene entity route content))
  ([scene entity route content]
   (swap! (get @messages scene)
          conj {:message/entity entity
                :message/route route
                :message/content content})))

(defn scenes []
  (set (keys (:essen/scenes @state/state))))

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

