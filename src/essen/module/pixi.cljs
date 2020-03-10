(ns essen.module.pixi
  (:require
   ["pixi.js" :as PIXI :refer [extras]]
   [clojure.spec.alpha :as s]
   [integrant.core :as ig]
   [essen.system.scene :as scene]
   [essen.module.pixi.state :refer [state sheets textures animations]]
   [essen.module.pixi.render :as render]
   [essen.module.pixi.component.sprite :as component.sprite]))

(defn- spritesheet-loaded [{::keys [spritesheet name scene-from scene-to]}]
  (let [sheet
        (-> (.-shared PIXI/Loader)
            (.-resources)
            (js->clj)
            (get spritesheet))]

    (swap! sheets assoc name sheet)
    (swap! textures assoc name (js->clj (.-textures sheet)))
    (swap! animations assoc name (js->clj (.-animations (.-spritesheet sheet)))))

  (scene/stop! scene-from)
  (scene/start! scene-to))

(defmethod ig/pre-init-spec ::load-spritesheet [_]
  (s/keys :req [::spritesheet
                ::name
                ::scene-from
                ::scene-to]))

(defmethod ig/init-key ::load-spritesheet [_ {::keys [spritesheet] :as opts}]
  (-> (.-shared PIXI/Loader)
      (.add spritesheet)
      (.load (partial spritesheet-loaded opts))))

(defmethod ig/init-key ::add-sprite [_ {::keys [sprite scene] :as opts}]
  (let [container (-> @state :pixi/stage scene :stage/container)
        sprite (PIXI/Sprite. (get-in @textures sprite))]
    (.addChild container sprite)))

(defmethod ig/init-key ::add-animation [_ {::keys [sprite scene] :as opts}]
  (let [container (-> @state :pixi/stage scene :stage/container)
        sprite (PIXI/AnimatedSprite. (clj->js (get-in @animations sprite)))]

    (set! (.-animationSpeed sprite) 0.167)
    (set! (.-x sprite) 100)
    (set! (.-y sprite) 100)
    (.play sprite)
    (.addChild container sprite)))

(def config
  (merge
   component.sprite/config))

(def module
  {:essen/setup render/setup
   :essen/stage-start render/stage-start
   :essen/stage-stop render/stage-stop
   :essen/stage-suspend render/stage-suspend})
