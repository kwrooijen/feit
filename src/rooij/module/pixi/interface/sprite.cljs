(ns rooij.module.pixi.interface.sprite
  (:require
   ["pixi.js" :as PIXI]
   [rooij.interface.general-2d.position :refer [RooijGeneral2DPosition]]
   [rooij.interface.graphics-2d.sprite :refer [RooijGraphics2DSprite]]
   [rooij.module.pixi.state :as state]))

(defrecord PixiGraphics2DSprite [sprite initial-textures x y]
  RooijGraphics2DSprite
  (play! [this spritesheet animation]
    (set! (.-textures sprite) (state/spritesheet-animation-texture spritesheet animation))
    (set! (.-loop sprite) false)
    (set! (.-onComplete sprite)
          (fn []
            (set! (.-textures sprite) initial-textures)
            (set! (.-loop sprite) true)
            (.play sprite)))
    (.play sprite)))

(extend-protocol RooijGeneral2DPosition
  PixiGraphics2DSprite
  (set-position [{:keys [sprite] :as this} x y angle]
    (set! (.. sprite -position -x) x)
    (set! (.. sprite -position -y) y)
    (set! (.. sprite -rotation) angle)
    (assoc this :x y :y y)))

(defn spritesheet-animated-sprite [{:spritesheet/keys [name animation]}]
  (let [textures (state/spritesheet-animation-texture name animation)
        sprite  (PIXI/AnimatedSprite. textures)]
    (.play sprite)
    (map->PixiGraphics2DSprite
     {:sprite sprite
      :initial-textures textures})))

(defn spritesheet-static-sprite [{:spritesheet/keys [name texture]}]
  (let [texture (state/spritesheet-static-texture name texture)]
    (map->PixiGraphics2DSprite
     {:sprite (PIXI/AnimatedSprite. #js [texture])
      :initial-textures [texture]})))

(defn texture-static-sprite [{:texture/keys [name]}]
  (let [texture (-> state/loader .-resources (aget name) .-texture)]
    (map->PixiGraphics2DSprite
     {:sprite (PIXI/AnimatedSprite. #js [texture])
      :initial-textures [texture]})))

(defn ->sprite [opts]
  (cond
    (:spritesheet/animation opts) (spritesheet-animated-sprite opts)
    (:spritesheet/texture opts)   (spritesheet-static-sprite opts)
    (:texture/name opts)          (texture-static-sprite opts)))

(defn make [{:context/keys [scene-key] :as opts}]
  (let [{:keys [sprite] :as state} (->sprite opts)]
    (set! (.-animationSpeed sprite) 0.167)
    (set! (.-x sprite) 100)
    (set! (.-y sprite) 100)
    (set! (.. sprite -scale -x) 2)
    (set! (.. sprite -scale -y) 2)
    (.set (.-anchor sprite) 0.5)
    (.addChild (state/get-scene scene-key) sprite)
    state))
