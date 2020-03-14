(ns essen.module.pixi.component.sprite
  (:require
   [essen.module.pixi.state :refer [state sheets textures animations]]
   ["pixi.js" :as PIXI]
   [clojure.spec.alpha :as s]
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]))

;; (defmethod ig/pre-init-spec :component.pixi/sprite [_]
;;   (s/keys :req [:component.pixi/spritesheet]))

(defmethod ig/prep-key :component.pixi/sprite [_ opts]
  (meta-merge opts
              {:component/handlers [(ig/ref :handler.pixi.sprite/play)
                                    (ig/ref :handler.pixi.sprite/set-pos)]}))

(defmethod ig/init-key :component.pixi/sprite
  [_ {:component/keys [sprite pos] :as opts}]
  (let [scene-key (-> opts :scene/opts :scene/key)
        container (get-in @state [:pixi/stage scene-key :stage/container])
        textures (get-in @animations sprite)
        sprite (PIXI/AnimatedSprite. textures)]
    (set! (.-animationSpeed sprite) 0.167)
    (set! (.-x sprite) (:x pos))
    (set! (.-y sprite) (:y pos))
    (.play sprite)
    (.addChild container sprite)
    {:pixi.sprite/sprite sprite
     :pixi.sprite/initial-textures textures}))

(defmethod ig/suspend-key! :component.pixi/sprite [_ {:component/keys [state]}]
  (.destroy (:pixi.sprite/sprite state)))

(defmethod ig/init-key :handler.pixi.sprite/play [_ opts]
  (fn handler-pixi-sprite--play
    [_context
     {:event/keys [animation]}
     {:pixi.sprite/keys [sprite initial-textures] :as state}]
    (set! (.-textures sprite) (get-in @animations animation))
    (.play sprite)
    (set! (.-loop sprite) false)
    (set! (.-onComplete sprite) (fn []
                                  (set! (.-textures sprite) initial-textures)
                                  (set! (.-loop sprite) true)
                                  (.play sprite)))
    state))

(defmethod ig/init-key :handler.pixi.sprite/set-pos [_ opts]
  (fn [_context
       {:event/keys [x y]}
       {:pixi.sprite/keys [sprite] :as state}]
    (set! (.-x sprite) x)
    (set! (.-y sprite) y)
    state))

;; TODO Modules should get a :module/components key (maybe). which makes sure this
;; gets derived. It's probably not logical to define the components here.
(derive :component.pixi/sprite :essen/component)

(def config
  {[:essen/handler :handler.pixi.sprite/play] {}
   [:essen/handler :handler.pixi.sprite/set-pos] {}})
