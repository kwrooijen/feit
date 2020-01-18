(ns cardo.config
  (:require
   [integrant.core :as ig]
   [phaser]))

(def debug?
  ^boolean goog.DEBUG)

(def config
  {:essen/game
   {:type js/Phaser.AUTO
    :backgroundColor 0xecf0f1
    :physics {:default :arcade}
    :tweenSpeed 2000
    :parent "game"
    :scene [(ig/ref :scene/battle)]}})
