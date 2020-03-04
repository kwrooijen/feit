(ns cardo.core
  (:require
   [essen.core :as essen :refer [emit!]]
   [essen.keyboard]
   [essen.ticker :as ticker]
   [essen.middleware :as middleware]
   [reagent.core :as reagent]
   [cardo.views :as views]
   [essen.module.pixi :as pixi]
   [cardo.config :as config]
   [integrant.core :as ig]))

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
   {:essen/setup pixi/setup
    :essen/stage-start pixi/stage-start
    :essen/stage-stop pixi/stage-stop
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
