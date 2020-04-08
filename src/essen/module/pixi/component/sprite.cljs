(ns essen.module.pixi.component.sprite
  (:require
   [essen.module.pixi.state :as state]
   ["pixi.js" :as PIXI]
   [meta-merge.core :refer [meta-merge]]
   [integrant.core :as ig]))

(defmethod ig/init-key :component/sprite
  [_ {:sprite/keys [file x y]
      :context/keys [scene-key]}]
  (let [texture (-> state/loader .-resources (aget file) .-texture)
        sprite (PIXI/Sprite. texture)]
    (set! (.-x sprite) x)
    (set! (.-y sprite) y)
    (.addChild (state/get-scene scene-key)
               sprite)
    sprite))

(def config {[:essen/component :component/sprite] {}})

(defn spritesheet-animated-sprite [{:spritesheet/keys [name animation]}]
  (let [textures (state/spritesheet-animation-texture name animation)
        sprite  (PIXI/AnimatedSprite. textures)]
    (.play sprite)
    {:pixi.sprite/sprite sprite
     :pixi.sprite/initial-textures textures}))

(defn spritesheet-static-sprite [{:spritesheet/keys [name texture]}]
  (let [texture (state/spritesheet-static-texture name texture)]
    {:pixi.sprite/sprite (PIXI/AnimatedSprite. #js [texture])
     :pixi.sprite/initial-textures [texture]}))

(defn texture-static-sprite [{:texture/keys [name]}]
  (let [texture (-> state/loader .-resources (aget name) .-texture)]
    {:pixi.sprite/sprite (PIXI/AnimatedSprite. #js [texture])
     :pixi.sprite/initial-textures [texture]}))

(defn ->sprite [opts]
  (cond
    (:spritesheet/animation opts) (spritesheet-animated-sprite opts)
    (:spritesheet/texture opts)   (spritesheet-static-sprite opts)
    (:texture/name opts)          (texture-static-sprite opts)))

(defmethod ig/prep-key :graphics-2d.component/sprite [_ opts]
  (meta-merge
   {:component/handlers [(ig/ref :handler.pixi.sprite/play)]}
   opts))

(defmethod ig/init-key :graphics-2d.component/sprite
  [_ {:context/keys [scene-key] :as opts}]
  (let [{:pixi.sprite/keys [sprite] :as state} (->sprite opts)]
    (set! (.-animationSpeed sprite) 0.167)
    (set! (.-x sprite) 100)
    (set! (.-y sprite) 100)
    (set! (.. sprite -scale -x) 2)
    (set! (.. sprite -scale -y) 2)
    (.set (.-anchor sprite) 0.5)
    (.addChild (state/get-scene scene-key) sprite)
    state))

;; (defmethod ig/suspend-key! :component.pixi/sprite [_ {:component/keys [state]}]
;;   (.destroy (:pixi.sprite/sprite state)))

(defmethod ig/init-key :handler.pixi.sprite/play [_ opts]
  (fn handler-pixi-sprite--play
    [_context
     {:event/keys [spritesheet animation]}
     {:pixi.sprite/keys [sprite initial-textures] :as state}]
    (set! (.-textures sprite) (state/spritesheet-animation-texture spritesheet animation))
    (set! (.-loop sprite) false)
    (set! (.-onComplete sprite) (fn []
                                  (set! (.-textures sprite) initial-textures)
                                  (set! (.-loop sprite) true)
                                  (.play sprite)))
    (.play sprite)
    state))

;; (defmethod ig/init-key :handler.pixi.sprite/set-pos [_ opts]
;;   (fn [_context
;;        {:event/keys [x y]}
;;        {:pixi.sprite/keys [sprite] :as state}]
;;     (set! (.-x sprite) x)
;;     (set! (.-y sprite) y)
;;     state))

;; (defmethod ig/init-key :handler.pixi.sprite/set-rotation [_ opts]
;;   (fn [_context
;;        {:event/keys [rotation]}
;;        {:pixi.sprite/keys [sprite] :as state}]
;;     (set! (.-rotation sprite) rotation)
;;     state))

;; (def config
;;   {[:essen/component :component.pixi/sprite]
;;    {:component/handlers [(ig/ref :handler.pixi.sprite/play)
;;                          (ig/ref :handler.pixi.sprite/set-pos)
;;                          (ig/ref :handler.pixi.sprite/set-rotation)]}
;;    [:essen/handler :handler.pixi.sprite/play] {}
;;    [:essen/handler :handler.pixi.sprite/set-pos] {}
;;    [:essen/handler :handler.pixi.sprite/set-rotation] {}})
