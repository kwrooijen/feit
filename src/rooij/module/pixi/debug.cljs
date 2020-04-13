(ns rooij.module.pixi.debug
  (:require
   ["pixi.js" :as PIXI]
   [rooij.module.pixi.state :as state]))

(defonce wireframe (atom (PIXI/Graphics.)))

(defn ->PixiPoint [{:keys [x y]}]
  (PIXI/Point. x y))

(defn draw-wireframe [wires scene-key]
  (let [container (state/get-scene scene-key)
        graphics (PIXI/Graphics.)]
    (set! (.-zOrder graphics) js/Number.MAX_SAFE_INTEGER)
    (.destroy @wireframe)
    (.lineStyle graphics 1 0xFF0000)
    (doseq [wire wires]
      (.drawPolygon graphics (clj->js (map ->PixiPoint wire))))
    (.addChild container graphics)
    (reset! wireframe graphics)))
