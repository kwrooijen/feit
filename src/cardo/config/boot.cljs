(ns cardo.config.boot)

(def config
  ;; TODO See if you can somehow create exception if scene key doesn't exist
  {[:essen/scene :scene/boot]
   {:essen.scene/config
    {:active true}

    :essen.scene/preload
    {[:essen.obj/load :load/assets]
     [[:image "bg" "images/bg.jpg"]]

     [:essen.obj/load]
     [[:multiatlas "atlas" "images/adventure/spritesheet/adventure.json" "images/adventure/spritesheet/"]]}

    :essen.scene/create
    {[:essen.obj/scene :boot/to-battle]
     [[:start "battle" {}]]}}})
