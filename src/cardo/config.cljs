(ns cardo.config
  (:require [integrant.core :as ig]
            [phaser]))

(def config
  {:essen/core
   {:essen/game {:type js/Phaser.AUTO
                 :backgroundColor 0xecf0f1
                 :physics {:default :arcade}
                 :tweenSpeed 2000
                 :scene [(ig/ref :scene/boot)]}}

   ;; Scene.Boot

   ;; Phaser Example:
   ;; http://labs.phaser.io/edit.html?src=src/camera/follow%20user%20controlled%20sprite.js&v=3.21.0
   [:essen/scene :scene/boot]

   {:essen.scene/preload
    {[:essen.scene/load :load/assets]
     {:essen/methods [[:image "bg" "images/bg.jpg"]
                      [:image "block" "images/block.png"]]}}

    :essen.scene/create
    {[:essen.scene/state :game/player]
     (ig/ref :boot/player)

     [:essen.scene/state :game/cursor]
     (ig/ref :boot/cursor)


     [:essen.scene/add :add/bg1]
     {:essen/methods [[:image 0 0 "bg"]
                      [:set-origin 0]]}

     [:essen.scene/add :add/bg2]
     {:essen/methods [[:image 1920 0 "bg"]
                      [:set-origin 0]
                      [:set-flip-x true]]}

     [:essen.scene/add :add/bg3]
     {:essen/methods [[:image 0 1080 "bg"]
                      [:set-origin 0]
                      [:set-flip-y true]]}

     [:essen.scene/add :add/bg4]
     {:essen/methods [[:image 1920 1080 "bg"]
                      [:set-origin 0]
                      [:set-flip-x true]
                      [:set-flip-y true]]}

     [:essen.scene/cameras.main :boot/camera]
     {:essen/methods [[:set-bounds 0 0 (* 1920 2) (* 1080 2)]
                      [:start-follow (ig/ref :boot/player) true 0.05 0.05]]}

     [:essen.scene/physics.world :boot/world]
     {:essen/methods [[:set-bounds 0 0 (* 1920 2) (* 1080 2)]]}


     [:essen.scene/input.keyboard :boot/cursor]
     {:essen/methods [[:create-cursor-keys]]}

     [:essen.scene/physics.add :boot/player]
     {:essen/methods [[:image 900 300 "block"]
                      [:set-collide-world-bounds true]]}}

    :essen.scene/update
    {:essen.scene.update/list
     [(ig/ref :my/updater)]

     :my/updater {}}}})
