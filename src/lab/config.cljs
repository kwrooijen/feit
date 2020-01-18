(ns lab.config
  (:require [integrant.core :as ig]
            [phaser]))

(def config
  {:essen/core
   {:essen/game
    {:type js/Phaser.AUTO
     :backgroundColor 0xecf0f1
     :physics {:default :arcade}
     :tweenSpeed 2000
     :scene [(ig/ref :scene/boot)]}}

   ;; Scene.Boot

   ;; Phaser Example:
   ;; http://labs.phaser.io/edit.html?src=src/camera/follow%20user%20controlled%20sprite.js&v=3.21.0

   [:essen/scene :scene/boot]
   {:essen.scene/config
    {:active true}

    :essen.scene/preload
    {[:essen.obj/load :load/assets]
     [[:image "bg" "images/bg.jpg"]
      [:image "block" "images/block.png"]]}

    :essen.scene/create
    {[:essen.scene/state :game/player] (ig/ref :boot/player)
     [:essen.scene/state :game/cursor] (ig/ref :boot/cursor)

     [:essen.obj/add :add/bg1] [[:set-bg 0 0 false false]]
     [:essen.obj/add :add/bg2] [[:set-bg 1920 0 true false]]
     [:essen.obj/add :add/bg3] [[:set-bg 0 1080 false true]]
     [:essen.obj/add :add/bg4] [[:set-bg 1920 1080 true true]]

     [:essen.obj/cameras.main :boot/camera]
     [[:set-bounds 0 0 (* 1920 2) (* 1080 2)]
      [:start-follow (ig/ref :boot/player) true 0.05 0.05]]

     [:essen.obj/physics.world :boot/world] [[:set-bounds 0 0 (* 1920 2) (* 1080 2)]]

     [:essen.obj/input.keyboard :boot/cursor] [[:create-cursor-keys]]

     [:essen.obj/physics.add :boot/player]
     [[:image 900 300 "block"]
      [:set-collide-world-bounds true]
      [:set-depth 1]]}

    :essen.scene/update
    {:essen.scene.update/list
     [(ig/ref :my/updater)]

     :my/updater {}}}})
