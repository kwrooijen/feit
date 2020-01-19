(ns cardo.config
  (:require
   [integrant.core :as ig]
   [phaser]

   [cardo.config.boot :as config.boot]
   [cardo.config.battle :as config.battle]))

(def debug?
  ^boolean goog.DEBUG)

(def config
  (merge
   {:essen/game
    {:type js/Phaser.AUTO
     :backgroundColor 0xecf0f1
     :physics {:default :arcade}
     :tweenSpeed 2000
     :parent "game"
     :scene [(ig/ref :scene/boot)
             (ig/ref :scene/battle)]}}
   config.boot/config
   config.battle/config))
