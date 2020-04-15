(ns rooij.module.pixi.debug
  (:require
   ["pixi.js" :as PIXI]
   [rooij.module.pixi.state :as state]))

(defonce wireframe (atom (PIXI/Graphics.)))

(defn draw-wireframe [scene-key vectors]
  (let [graphics (PIXI/Graphics.)]
    (set! (.-zOrder graphics) js/Number.MAX_SAFE_INTEGER)
    (.destroy @wireframe)
    (.lineStyle graphics 1 0xFF0000)
    (doseq [[x y] vectors]
      (.drawPolygon graphics (clj->js (PIXI/Point. x y))))
    (.addChild (state/get-scene scene-key) graphics)
    (reset! wireframe graphics)))
