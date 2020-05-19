(ns feit.module.pixi.debug
  (:require
   ["pixi.js" :as PIXI]
   [feit.module.pixi.state :as state]))

(defonce wireframe (atom (PIXI/Graphics.)))

(defn ->PixiPoint [[x y]]
  (PIXI/Point. x y))

(defn draw-wireframe [scene-key wires]
  (let [graphics (PIXI/Graphics.)]
    (when-let [scene (state/get-scene scene-key)]
      (set! (.-zOrder graphics) js/Number.MAX_SAFE_INTEGER)
      (.destroy @wireframe)
      (.lineStyle graphics 1 0xFF0000)
      (doseq [wire wires]
        (.drawPolygon graphics (clj->js (map ->PixiPoint wire))))
      (.addChild scene graphics)
      (reset! wireframe graphics))))
