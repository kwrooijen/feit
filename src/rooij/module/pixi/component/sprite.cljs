(ns rooij.module.pixi.component.sprite
  (:require
   [rooij.module.pixi.state :as state]
   ["pixi.js" :as PIXI]
   [integrant.core :as ig]
   [rooij.interface.graphics-2d.core :refer [RooijSprite2D]]))

(defn -play! [{:keys [sprite initial-textures]} spritesheet animation]
  (set! (.-textures sprite) (state/spritesheet-animation-texture spritesheet animation))
  (set! (.-loop sprite) false)
  (set! (.-onComplete sprite) (fn []
                                (set! (.-textures sprite) initial-textures)
                                (set! (.-loop sprite) true)
                                (.play sprite)))
  (.play sprite))

(defrecord SpritePixi [sprite initial-textures]
  RooijSprite2D
  (play! [this spritesheet animation]
    (-play! this spritesheet animation)))

(defn spritesheet-animated-sprite [{:spritesheet/keys [name animation]}]
  (let [textures (state/spritesheet-animation-texture name animation)
        sprite  (PIXI/AnimatedSprite. textures)]
    (.play sprite)
    (map->SpritePixi
     {:sprite sprite
      :initial-textures textures})))

(defn spritesheet-static-sprite [{:spritesheet/keys [name texture]}]
  (let [texture (state/spritesheet-static-texture name texture)]

    (map->SpritePixi
     {:sprite (PIXI/AnimatedSprite. #js [texture])
      :initial-textures [texture]})))

(defn texture-static-sprite [{:texture/keys [name]}]
  (let [texture (-> state/loader .-resources (aget name) .-texture)]
    (map->SpritePixi
     {:sprite (PIXI/AnimatedSprite. #js [texture])
      :initial-textures [texture]})))

(defn ->sprite [opts]
  (cond
    (:spritesheet/animation opts) (spritesheet-animated-sprite opts)
    (:spritesheet/texture opts)   (spritesheet-static-sprite opts)
    (:texture/name opts)          (texture-static-sprite opts)))

(defmethod ig/init-key :graphics-2d.component/sprite
  [_ {:context/keys [scene-key] :as opts}]
  (let [{:keys [sprite] :as state} (->sprite opts)]
    (set! (.-animationSpeed sprite) 0.167)
    (set! (.-x sprite) 100)
    (set! (.-y sprite) 100)
    (set! (.. sprite -scale -x) 2)
    (set! (.. sprite -scale -y) 2)
    (.set (.-anchor sprite) 0.5)
    (.addChild (state/get-scene scene-key) sprite)
    state))
