(ns essen.module.pixi.entity
  (:require
   [clojure.string :as string]
   [essen.core :as essen]
   [essen.module.pixi.state :as state]
   [integrant.core :as ig]))

(defn process-asset-alias
  [alias {:g2d.asset-loader/keys [prefix discard-extension? discard-path?]}]
  (or (and (qualified-keyword? alias) (str alias))
      (cond-> alias
        discard-extension? (-> (string/split #"(.*)\.[^.]+$") last)
        discard-path?      (-> (string/split #"/") last)
        true               (->> (str prefix)))))

(defn ->vector [v]
  (if (vector? v) v (vector v)))

(defmethod ig/init-key :graphics-2d.entity/asset-loader
  [_ {:g2d.asset-loader/keys [files next-scene]
      :context/keys [scene-key] :as opts}]
  (doseq [[alias file] (mapv ->vector files)]
    (.add state/loader (process-asset-alias alias opts) (or file alias)))
  (.load state/loader #(essen/transition-scene scene-key next-scene)))

