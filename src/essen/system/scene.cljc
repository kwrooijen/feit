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
   (state/reset-events! scene-key)
   (let [config (assoc (state/config) [:it/const :scene/opts] (assoc opts :scene/key scene-key)
                       [:it/const :entity/opts] nil)]
     (->  config
          (es/init [scene-key])
          (it/find-derived-value scene-key)
          (update :scene/entities (partial entities-fn config))
          (state/save-scene!)))))

(defn halt! [scene-key]
  (essen.render/run scene-key :essen/stage-halt)
  (doseq [[_ entity] (:scene/entities @(state/get-scene scene-key))]
    (entity/halt! entity))
  (state/reset-events! scene-key)
  (state/reset-state! scene-key))

;; TODO Need to implement halt / suspend / resume properly
