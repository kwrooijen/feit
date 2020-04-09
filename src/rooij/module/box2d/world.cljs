(ns rooij.module.box2d.world)

(defn bodies [world]
  (let [bodies (volatile! [])]
    (loop [body (.getBodyList world)]
      (if body
        (do
          (vswap! bodies conj body)
          (recur (.getNext body)))
        @bodies))))

(defn fixtures [world]
  (let [fixtures (volatile! [])]
    (loop [fixture (.getFixtureList world)]
      (if fixture
        (do
          (vswap! fixtures conj fixture)
          (recur (.getNext fixture)))
        @fixtures))))
