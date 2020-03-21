(ns essen.system.scene
  (:require
   [essen.state :as state]
   [essen.util :refer [vec->map spy top-key]]
   [integrant-tools.core :as it]
   [essen.system.entity :as entity]
   [essen.system :as es]
   [integrant.core :as ig]
   [essen.render]))

(defn- entities-fn [config entities]
  (vec->map (for [entity (flatten entities)]
              (-> config
                  (assoc [:it/const :context/entity] entity)
                  (entity/start entity)))
            :entity/key))

(defmethod es/init-key :essen/scene [k opts]
  (-> (ig/init-key k opts)
      (assoc :scene/key (top-key k))))

(defn start!
  ([scene-key] (start! scene-key {} {}))
  ([scene-key opts] (start! scene-key opts {}))
  ([scene-key opts extra]
   (when-not (:dev extra)
     (essen.render/run scene-key :essen/stage-start))
   (state/reset-events! scene-key)
   (let [config (assoc (state/config)
                       [:it/const :context/scene] scene-key
                       ;; TODO See if we can get rid of this, and not have to init scene?
                       [:it/const :context/entity] nil
                       [:it/const :scene/opts] opts)]
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
