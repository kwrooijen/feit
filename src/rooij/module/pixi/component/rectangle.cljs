(ns rooij.module.pixi.component.rectangle
  (:require
   [integrant.core :as ig]
   [rooij.module.pixi.state :as state]
   ["pixi.js" :as PIXI]))

(defmethod ig/init-key :graphics-2d.component/rectangle
  [_ {:shape/keys [x y w h fill] :context/keys [scene-key]}]
  (let [rectangle (.from PIXI/Sprite  PIXI/Texture.WHITE)]
    (set! (.-x rectangle) x)
    (set! (.-y rectangle) y)
    (set! (.-width rectangle) w)
    (set! (.-height rectangle) h)
    (set! (.-tint rectangle) fill)
    (.addChild (state/get-scene scene-key) rectangle)
    rectangle))
