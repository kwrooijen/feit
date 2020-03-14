(ns essen.module.pixi
  (:require
   ["pixi.js" :as PIXI]
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [essen.system.scene :as scene]
   [essen.module.pixi.state :refer [sheets textures animations]]
   [essen.module.pixi.render :as render]
   [essen.module.pixi.component.sprite :as component.sprite]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]))

(defn js-keys->clj-keys [o]
  (transform [MAP-VALS] clj->js (js->clj o)))

(defn- spritesheet-loaded
  [{::keys [spritesheet name transition] :as opts}]
  (let [sheet
        (-> (.-shared PIXI/Loader)
            (.-resources)
            (aget spritesheet))]
    (swap! sheets assoc name sheet)
    (swap! textures assoc name (js-keys->clj-keys (.-textures sheet)))
    (swap! animations assoc name (js-keys->clj-keys (.-animations (.-spritesheet sheet)))))

  (scene/stop! (-> opts :scene/opts :scene/key))
  (scene/start! transition))

(defmethod ig/pre-init-spec ::load-spritesheet [_]
  (s/keys :req [::spritesheet
                ::name
                ::transition]))

(defmethod ig/init-key ::load-spritesheet [_ {::keys [spritesheet] :as opts}]
  (-> (.-shared PIXI/Loader)
      (.add spritesheet)
      (.load (partial spritesheet-loaded opts))))

(def config
  (merge
   component.sprite/config))

(def module
  {:essen/setup render/setup
   :essen/stage-start render/stage-start
   :essen/stage-stop render/stage-stop
   :essen/stage-resume render/stage-resume
   :essen/stage-suspend render/stage-suspend})
