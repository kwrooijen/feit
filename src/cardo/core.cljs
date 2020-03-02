(ns cardo.core
  (:require
   [essen.core :as essen :refer [emit!]]
   [essen.keyboard]
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


