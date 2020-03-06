(ns cardo.core
  (:require
   [essen.core :as essen :refer [emit!]]
   [essen.keyboard]
   [essen.system.ticker :as ticker]
   [essen.system.middleware :as middleware]
   [reagent.core :as reagent]
   [cardo.views :as views]
   [essen.module.pixi :as pixi]
   [cardo.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (reagent/render [views/main-panel]
    (.getElementById js/document "interface")))

(defn ^:export init []
  (dev-setup)
  (mount-root)
  (essen/setup
   {:essen.module/render pixi/module
    :essen/config config/config})

  (essen.keyboard/disable-tabbing!)
  (essen.keyboard/add-event-listener "keydown" essen/emit-keydown!)
  (essen.keyboard/add-event-listener "keyup"   essen/emit-keyup!)
  (essen/start-scene :scene/start)
  (emit! :scene/start :entity/player :handler.stats/attack {:event/damage 2})
  (comment
    ;; (pixi/stage-stop :stage/loading)
    ))

(defn stop []
  ;; (essen.core/suspend!)
  )

(defn start []
  ;; (essen.core/resume config/config)
  )




(comment
  (ticker/remove! :scene/start
                  :entity/player
                  :component/stats
                  :ticker.stats/poisoned)

  (ticker/add! :scene/start
                 :entity/player
                 :component/stats
                 :ticker.stats/poisoned
                 {:ticker/ticks 2
                  :ticker/damage 30})

  (middleware/add! :scene/start
                     :entity/player
                     :component/stats
                     :handler.stats/attack
                     :middleware.stats/invincible
                     {})

  (middleware/remove! :scene/start
                      :entity/player
                      :component/stats
                      :handler.stats/attack
                      :middleware.stats/invincible))
