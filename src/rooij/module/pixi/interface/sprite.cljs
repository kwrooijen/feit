(ns rooij.module.pixi.interface.sprite
  (:require
   ["pixi.js" :as PIXI]
   [rooij.interface.general-2d.position :refer [RooijGeneral2DPosition]]
   [rooij.interface.graphics-2d.sprite :refer [RooijGraphics2DSprite]]
   [rooij.module.pixi.state :as state]))

(defn -play [{:keys [sprite initial-textures] :as this} spritesheet [animation & chain]]
  (set! (.-textures sprite) (state/spritesheet-animation-texture spritesheet animation))
  (set! (.-running-animation sprite) animation)
  (set! (.-loop sprite) false)
  (set! (.-onComplete sprite)
        (fn []
          (if (seq chain)
            (-play this spritesheet chain)
            (do
              (set! (.-textures sprite) initial-textures)
              (set! (.-loop sprite) true)
              (set! (.-running-animation sprite) :initial)
              (.play sprite)))))
  (.play sprite))

(defn -play-loop [{:keys [sprite] :as this} spritesheet [animation & chain]]
  (set! (.-textures sprite) (state/spritesheet-animation-texture spritesheet animation))
  (set! (.-running-animation sprite) animation)
  (set! (.-loop sprite) (not (boolean (seq chain))))
  (when (seq chain)
    (set! (.-onComplete sprite)
          (fn []
            (-play-loop this spritesheet chain))))
  (.play sprite))

(defn ->vec [v]
  (if (vector? v) v [v]))

(defrecord PixiGraphics2DSprite [sprite initial-textures x y flip]
  RooijGraphics2DSprite
  (play [this spritesheet animations]
    (-play this spritesheet (->vec animations))
    this)

  (play-loop [this spritesheet animation opts]
    (when-not (= (.-running-animation sprite) animation)
      (-play-loop this spritesheet (->vec animation)))
    this)

  (stop-loop [this animation]
    (when (= (.-running-animation sprite) animation)
      (set! (.-textures sprite) initial-textures)
      (set! (.-loop sprite) true)
      (set! (.-running-animation sprite) :initial)
      (.play sprite))
    this)

  (flip [this x y]
    (when (or (and x (pos-int? (.. sprite -scale -x)))
              (and (not x) (neg-int? (.. sprite -scale -x))))
      (set! (.. sprite -scale -x) (* -1 (.. sprite -scale -x))))
    (when (or (and y (pos-int? (.. sprite -scale -y)))
              (and (not y) (neg-int? (.. sprite -scale -y))))
      (set! (.. sprite -scale -y) (* -1 (.. sprite -scale -y))))
    (-> this
        (assoc-in [:flip :x] x)
        (assoc-in [:flip :y] y))))

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
      :initial-textures textures
      :flip {:x false :y false}})))

(defn spritesheet-static-sprite [{:spritesheet/keys [name texture]}]
  (let [texture (state/spritesheet-static-texture name texture)]
    (map->PixiGraphics2DSprite
     {:sprite (PIXI/AnimatedSprite. #js [texture])
      :initial-textures [texture]
      :flip {:x false :y false}})))

(defn texture-static-sprite [{:texture/keys [name]}]
  (let [texture (-> state/loader .-resources (aget name) .-texture)]
    (map->PixiGraphics2DSprite
     {:sprite (PIXI/AnimatedSprite. #js [texture])
      :initial-textures [texture]
      :flip {:x false :y false}})))

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
