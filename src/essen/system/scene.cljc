(ns essen.system.scene
  (:require
   [essen.state :as state]
   [essen.util :refer [vec->map spy]]
   [integrant-tools.core :as it]
   [essen.system.entity :as entity]
   [essen.system :as es]
   [integrant.core :as ig]
   [essen.render]))

(defn- entities-fn [config entities]
  (-> (map (partial entity/start config) (flatten entities))
      (vec->map :entity/key)))

(defmethod es/init-key :essen/scene [k opts]
  (-> (ig/init-key k opts)
      (assoc :scene/key (last k))))

(defn start!
  ([scene-key] (start! scene-key {} {}))
  ([scene-key opts] (start! scene-key opts {}))
  ([scene-key opts extra]
   (when-not (:dev extra)
     (essen.render/run scene-key :essen/stage-start))
   (let [config (assoc (state/config) [:it/const :scene/opts] (assoc opts :scene/key scene-key)
                       [:it/const :entity/opts] nil)]
     (->  config
          (it/init [:essen/init] [scene-key])
          (it/find-derived-value scene-key)
          (update :scene/entities (partial entities-fn config))
          (state/save-scene!)))
   (state/reset-events! scene-key)))

(defn stop! [scene-key]
  (essen.render/run scene-key :essen/stage-stop)
  (doseq [[_ entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/stop! entity))
  (state/reset-events! scene-key)
  (state/reset-state! scene-key))
