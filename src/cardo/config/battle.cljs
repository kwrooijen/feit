(ns cardo.config.battle
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [integrant.core :as ig]))

(def config
  {[:essen/scene :scene/battle]
   {:essen.scene/create
    {[:essen.scene/state :game/cursor]
     (ig/ref :battle/cursor)

     [:essen.scene/state :game/adventurer]
     (ig/ref :adventurer/sprite)

     [:essen.scene/state :attack/timer]
     (ig/ref :attack/timer-def)

     [:essen.scene/run :my/run]
     {}

     [:essen.obj/time :attack/timer-def]
     [[:add-event {:delay 2000 :loop true :callback (ig/ref :adventurer/timer)}]]

     :adventurer/timer {:adventurer (ig/ref :adventurer/sprite)}

     [:essen.obj/add :adventurer/sprite]
     [[:debug (ig/ref :adventurer.frames/idle)]
      [:debug (ig/ref :adventurer.frames/attack)]
      [:sprite 500 400 "atlas"]
      [:set-scale 5]
      [:set-depth 1]
      [:play "adventurer/idle"]]

     [:essen.obj/anims :adventurer.frames/idle]
     [[:create-anim "adventurer/idle" "adventurer-idle-2-" 3 -1 8]]

     [:essen.obj/anims :adventurer.frames/attack]
     [[:create-anim "adventurer/attack" "adventurer-attack1-" 4 0 14]]

     [:essen.obj/add :add/bg1] [[:set-bg 0 0 false false]]
     [:essen.obj/add :add/bg2] [[:set-bg 1920 0 true false]]
     [:essen.obj/add :add/bg3] [[:set-bg 0 1080 false true]]
     [:essen.obj/add :add/bg4] [[:set-bg 1920 1080 true true]]

     [:essen.obj/physics.world :battle/world] [[:set-bounds 0 0 (* 1920 2) (* 1080 2)]]

     [:essen.obj/input.keyboard :battle/cursor] [[:create-cursor-keys]]}

    :essen.scene/update
    {:essen.scene.update/list
     [(ig/ref :my/updater)]
     :my/updater {}}}})
