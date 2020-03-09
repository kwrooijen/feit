(ns essen.core
  (:require
   [clojure.spec.alpha :as s]
   [essen.state :as state :refer [input-messages messages game systems]]
   [essen.util :refer [vec->map spy]]
   [essen.system]
   [essen.system.component]
   [essen.system.entity]
   [essen.system.handler]
   [essen.system.keyboard]
   [essen.system.middleware]
   [essen.system.reactor]
   [essen.system.scene]
   [essen.system.ticker]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :as it.keyword]
   [integrant.core :as ig]
   [spec-signature.core :refer-macros [sdef]]))

(defn- render-run [scene-key type]
  ((-> @game :essen.module/render type) (:essen/config @game) scene-key))

(defmethod ig/init-key :essen/const [_ opts] opts)

(defn derive-components
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
           (map? v) (assoc :scene/opts (ig/ref :scene/opts)))))

(defn setup-essen-config [config]
  (->> config
       (reduce-kv add-scene-opts-ref {})
       (ig/prep)))

(defn setup [{:keys [:essen/config :essen.module/render] :as game-config}]
  (derive-components config)
  (reset! game (update game-config :essen/config setup-essen-config))
  ((:essen/setup render) config))

(defn emit!
  ([{:context/keys [scene entity]} route content]
   (emit! scene entity route content))
  ([scene entity route content]
   (swap! (get @messages scene)
          conj {:message/entity entity
                :message/route route
                :message/content content})))

(defn start-entity [config key]
  (let [ig-key (it/find-derived-key config key)
        dynamic?  (:dynamic (meta ig-key))
        v (it/find-derived-value config key)
        key (if dynamic? (conj ig-key (it.keyword/make-child key)) key)
        config (if dynamic? (assoc config key v) config)
        system (it/init config [:essen/init] [key :essen/component])]
    (with-meta
      (it/find-derived-value system key)
      {:system system})))

(defn halt-entity! [entity]
  (ig/halt! (:system (meta entity))))

(defn entities-fn [config entities]
  (-> (map (partial start-entity config) (flatten entities))
      (vec->map :entity/key)))

(defn start-scene
  ([scene-key] (start-scene scene-key {}))
  ([scene-key opts]
   (let [config (assoc (state/config) [:it/const :scene/opts] opts)]
     (->  config
          (it/init [:essen/init] [scene-key])
          (it/find-derived-value scene-key)
          (update :scene/entities (partial entities-fn config))
          (state/save-scene!)))
   (state/reset-events! scene-key)
   (render-run scene-key :essen/stage-start)))

(defn stop-scene [scene-key]
  (render-run scene-key :essen/stage-stop)
  (doseq [[_ entity] (:scene/entities @(state/get-scene scene-key))]
    (halt-entity! entity))
  (state/reset-events! scene-key)
  (state/reset-state! scene-key))

(defn resume-scene [scene-key]
  ;; FIXME This doesn't work
  ;; Would be nice to keep the state, but replacee the :*/fn functions.
  (-> (state/config)
      (it/prep [:it/prep-meta :ig/prep] [scene-key])
      (ig/resume (state/system scene-key) [scene-key])
      (state/save-system!  scene-key))
  (state/reset-events! scene-key)
  (state/save-state! scene-key)
  (render-run scene-key :essen/stage-resume))

(defn suspend-scene [scene-key]
  (render-run scene-key :essen/stage-suspend))

(defn scenes []
  (set (keys (:essen/scenes @state/state))))

(defn suspend! []
  (doseq [scene (scenes)]
    (suspend-scene scene)))

(defn resume [config]
  (swap! game assoc :essen/config (ig/prep config))
  (doseq [scene (scenes)]
    ;; TODO scene/opts get lost after `resume`..
    ;; Does that actually matter once we implement a proper resume?
    ;; Right now it should be fixed though
    ;; FIXME `resume-scene` needs to eb fixed, and should be called here
    (start-scene scene)))

;; TODO Maybe use Specter to clean this logic up
(defn entities [scene-key entity-key]
  (->> (ig/find-derived (:scene/entities @(state/get-scene scene-key)) entity-key)
       (map (fn [[k v]] [k (:entity/components v)]))
       (map (fn [[k v]] [k (into {} (map (fn [[kk vv]] [kk (:component/state vv)]) v))]))
       (into {})))

(defn entity [scene-key entity-key]
  (get (entities scene-key entity-key) entity-key))
