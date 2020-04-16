(ns rooij.module.pixi.interface.rectangle
  (:require
   ["pixi.js" :as PIXI]
   [rooij.interface.general-2d.core :refer [RooijGeneral2DPosition]]
   [rooij.interface.graphics-2d.core :refer [RooijGraphics2DRectangle]]
   [rooij.module.pixi.state :as state]))

(defrecord PixiGraphics2DRectangle [body x y w h]
  RooijGraphics2DRectangle)

(extend-protocol RooijGeneral2DPosition
  PixiGraphics2DRectangle
  (set-position [{:keys [body] :as this} x y angle]
    (set! (.. body -position -x) x)
    (set! (.. body -position -y) y)
    (set! (.. body -rotation) angle)
    (assoc this :x y :y y)))

(defn make
  [{:shape/keys [x y w h fill] :context/keys [scene-key]}]
  (let [rectangle (.from PIXI/Sprite  PIXI/Texture.WHITE)]
    (set! (.-x rectangle) x)
    (set! (.-y rectangle) y)
    (set! (.-width rectangle) w)
    (set! (.-height rectangle) h)
    (set! (.-tint rectangle) fill)
    (set! (.. rectangle -anchor -x) 0.5)
    (set! (.. rectangle -anchor -y) 0.5)
    (.addChild (state/get-scene scene-key) rectangle)
    (map->PixiGraphics2DRectangle
     {:body rectangle :x x :y y :w w :h h})))
