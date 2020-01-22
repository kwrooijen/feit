(ns essen.state
  (:require
   [clojure.spec.alpha :as s]
   [spec-signature.core :refer-macros [sdef]]))

(defonce system (atom nil))
(defonce phaser-game (atom nil))
(defonce scene-queues (atom {}))

(sdef scenes [] (s/coll-of object?))
(defn scenes []
  (if @phaser-game
    (mapv #(.-scene %) (.. @phaser-game -scene -scenes))
    []))

(sdef scene-keys [] (s/coll-of :scene/key))
(defn scene-keys []
  (mapv #(.-key %) (scenes)))
