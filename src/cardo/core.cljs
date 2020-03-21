(ns cardo.core
  (:require
   [cardo.config :as config]
   [cardo.views :as views]
   [essen.core :as essen :refer [emit!]]
   [essen.dev]
   [essen.module.cljs.keyboard :as keyboard]
   [essen.module.pixi]
   [essen.module.matterjs]
   [essen.system.middleware :as middleware]
   [essen.system.scene :as scene]
   [essen.system.ticker :as ticker]
   [reagent.core :as reagent]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (reagent/render [views/main-panel]
    (.getElementById js/document "interface")))

(defn ^:export init []
  (dev-setup)
  (mount-root)
  (essen/setup config/config)

  (keyboard/disable-tabbing!)
  (keyboard/add-event-listeners!)

  (scene/start! :scene/load))

(defn suspend! []
  (essen.dev/suspend!))

(defn resume []
  (essen.dev/resume config/config))

(comment
  (ticker/remove! :scene/battle
                  :entity/player
                  :component/stats
                  :ticker.stats/poisoned)

  (ticker/add! :scene/battle
               :entity/player
               :component/stats
               :ticker.stats/poisoned
               {:ticker/ticks 40
                :ticker/damage 2})

  (middleware/add! :scene/start
                   :entity/player
                   :component/stats
                   :handler.stats/attack
                   :middleware.stats/invincible
                   {})

  (middleware/remove! :scene/start
                      :entity/yeti+11
                      :component/stats
                      :handler.stats/attack
                      :middleware.stats/invincible)
  ;;
  )
