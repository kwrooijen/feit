(ns feit.module.pixi.interface.rectangle
  (:require
   ["pixi.js" :as PIXI]
   [feit.module.pixi.util.position :as util.position]
   [feit.interface.general-2d.position :refer [FeitGeneral2DPosition]]
   [feit.interface.graphics-2d.rectangle :refer [FeitGraphics2DRectangle]]
   [feit.module.pixi.state :as state]))

(defrecord PixiGraphics2DRectangle [body x y w h]
  FeitGraphics2DRectangle
  (halt! [this]
    (.destroy (:body this))))
(extend-protocol FeitGeneral2DPosition
  PixiGraphics2DRectangle
  (set-position [{:keys [body] :as this} x y angle opts]
    (set! (.. body -position -x) (util.position/x-with-anchor body x opts))
    (set! (.. body -position -y) (util.position/y-with-anchor body y opts))
    (set! (.. body -rotation) angle)
    (assoc this :x x :y y)))

(defn make
  [{:shape/keys [w h fill]
    :position/keys [x y]
    :context/keys [scene-key]}]
  (let [rectangle (.from PIXI/Sprite PIXI/Texture.WHITE)]
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
