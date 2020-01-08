(ns cardo.config
  (:require [integrant.core :as ig]
            [phaser]))

(def config
  {:essen/core
   {:essen/game {:type js/Phaser.AUTO
                 :backgroundColor 0xecf0f1
                 :physics {:default :arcade}
                 :tweenSpeed 2000
                 :scene [(ig/ref :scene/foo)]}}

   ;; Scene.Boot

   ;; [:essen/scene :scene/boot]
   ;; {:kevin [(ig/ref :kevin/image)
   ;;          (ig/ref :load.image/bg)]}

   [:essen/scene :scene/foo]

   {:essen.scene/preload
    {[:essen.scene/load :load/bg]
     {:essen/methods [[:image "bg" "images/bg.jpg"]]}

     [:essen.scene/load :load/block]
     {:essen/methods [[:image "block" "images/block.png"]]}}


    :essen.scene/create
    {[:essen.scene/add :add/bg]
     {:essen/methods [[:image 0 0 "bg"]
                      [:set-flip-x true]]}
     }
    ;; {[:essen.scene/add :user/kevin]
    ;;  {:mijn-demi (ig/ref :user/demi)}

    ;;  [:essen.scene/add :user/demi]
    ;;  {:says "Hoi, ik ben Demi"}}



    ;; :essen.scene/update {}
    }


   ;; [:essen/a-scene :scene/bar]
   ;; {:create {:loaders [(ig/ref :my/kevin)]
   ;;           :my/kevin {:create-image "foo"}}}


   ;; [:essen/const :scene.create.foo/hoi]
   ;; {:something 123}

   ;; [:essen/scene :scene/boot]
   ;; {:essen.scene/create
   ;;  {:kevin (ig/ref :my/kevin)
   ;;   :poep (ig/ref :my/poep)
   ;;   :demi (ig/ref :my/demi)}

   ;;  :my/poep {:kevin (ig/ref :my/kevin)}
   ;;  :my/demi {:kevin (ig/ref :my/kevin)}
   ;;  :my/kevin {:create-image "foo"}}

   ;; :essen.scene/global
   ;; {:my/kevin {:create-image "foo"}}

   ;; :scene.boot/load
   ;; {:loaders [:] }

   ;; [:essen.module/add.image :kevin/image] {:x 0 :y 0 :texture "bg" :kevin (ig/ref :load.image/bg)}

   ;; [:essen.module/load.image :load.image/bg]
   ;; {:key "bg" :url "images/bg.jpg"}



   ;; [:essen.module/const :my/ground-tiles]
   ;; {:tilemap (essen/ref :my/tilemap)
   ;;  :apply (fn [{:keys [:tilemap]} ])}

   ;; [:essen.module/make.tilemap :my/tilemap]
   ;; {:key "map"}

   ;; {:key :scene/boot

   ;;  :modules/update [ ;; (ig/ref :my/update)
   ;;                   ;; (ig/ref :my/update2)
   ;;                   ]

   ;;  :modules/preload [ ;; (ig/ref :load.image/bg)
   ;;                    ;; (ig/ref :load.image/block)
   ;;                    ]

   ;;  :modules/create [ ;; (ig/ref :scene.boot/player)
   ;;                   ;; (ig/ref :scene.boot/cameras-main)
   ;;                   ;; (ig/ref :scene.boot/physics-world)
   ;;                   ;; (ig/ref :scene.boot/background1)
   ;;                   ;; (ig/ref :scene.boot/background2)
   ;;                   ;; (ig/ref :scene.boot/background3)
   ;;                   ;; (ig/ref :scene.boot/background4)
   ;;                   ;; (ig/ref :scene.boot/keyboard)
   ;;                   ]}


   ;; [:essen.module/input-keyboard :scene.boot/keyboard]
   ;; {:apply #(.createCursorKeys %)}


   ;; [:essen.module/physics.add.image :scene.boot/player]
   ;; {:x 400 :y 300 :texture "block"
   ;;  :apply #(.setCollideWorldBounds % true)}

   ;; ;; player.setCollideWorldBounds(true);

   ;; ;; this.cameras.main.startFollow(player, true, 0.25, 0.25);

   ;; ;; text = this.add.text(10, 10, 'Cursors to move', { font: '16px Courier', fill: '#00ff00' }).setScrollFactor(0);



   ;; [:essen.module/cameras-main :scene.boot/cameras-main]
   ;; {:player (ig/ref :scene.boot/player)
   ;;  :apply (fn [obj opts]
   ;;           (println "FRAME" (.-frame (:player opts)))
   ;;           (.. obj (startFollow (:player opts) true 0.25 0.25))
   ;;           (.setBounds obj 0 0 (* 1920 2) (* 1080 2)))}

   ;; [:essen.module/physics-world :scene.boot/physics-world]
   ;; {:apply #(.setBounds % 0 0 (* 1920 2) (* 1080 2))}

   ;; [:essen.module/add.image :scene.boot/background1]
   ;; {:x 0 :y 0 :texture "bg" :apply #(.setOrigin % 0)}

   ;; [:essen.module/add.image :scene.boot/background2]
   ;; {:x 1920 :y 0 :texture "bg"
   ;;  :apply #(-> % (.setOrigin 0) (.setFlipX true))}

   ;; [:essen.module/add.image :scene.boot/background3]
   ;; {:x 0 :y 0 :texture "bg"
   ;;  :apply #(-> % (.setOrigin 0) (.setFlipY true))}

   ;; [:essen.module/add.image :scene.boot/background4]
   ;; {:x 1920 :y 1080 :texture "bg"
   ;;  :apply #(-> % (.setOrigin 0) (.setFlipX true) (.setFlipY true))}

   ;; [:essen.module/load.image :load.image/bg]
   ;; {:key "bg" :url "images/bg.jpg"}

   ;; [:essen.module/load.image :load.image/block]
   ;; {:key "block" :url "images/block.png"}

   ;; [:essen.module/add.image :add.image/bg]
   ;; {:key "bg" :x 0 :y 0}

   ;; [:essen.module/add.image :add.image/block]
   ;; {:key "block" :x 0 :y 0}
   })
