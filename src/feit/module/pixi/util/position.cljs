(ns feit.module.pixi.util.position)

(defn x-with-anchor
  "Calculate the new x position with a possible :anchor/left
  of :anchor/right. if neither of these keys are set, the anchor will
  be placed in the center."
  [body x {:anchor/keys [left right]}]
  (cond
    left (- x (quot (.-width body) 2))
    right (+ x (quot (.-width body) 2))
    :else x))

(defn y-with-anchor
  "Calculate the new y position with a possible :anchor/top
  of :anchor/bottom. if neither of these keys are set, the anchor will be placed
  in the center."
  [body y {:anchor/keys [top bottom]}]
  (cond
    top (+ y (quot (.-height body) 2))
    bottom (- y (quot (.-height body) 2))
    :else y))
