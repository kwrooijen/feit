(ns essen.module.pixi.entity
  (:require
   [taoensso.timbre :as timbre]
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
  (timbre/info ::asset-loader {:files files})
  (.load state/loader #(essen/transition-scene scene-key next-scene)))

(defn- spritesheet-loaded
  [{:g2d.spritesheet-loader/keys [file name next-scene]
    :context/keys [scene-key]}]
  (-> state/loader
      (.-resources)
      (aget file)
      (->> (state/add-spritesheet! name)))
  (timbre/info ::spritesheet-loader {:name name :file file})
  (essen/transition-scene scene-key next-scene))

(defmethod ig/init-key :graphics-2d.entity/spritesheet-loader
  [_ {:g2d.spritesheet-loader/keys [file] :as opts}]
  (-> state/loader
      (.add file)
      (.load (partial spritesheet-loaded opts))))
