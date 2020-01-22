(ns cardo.core
  (:require
   [essen.core :as es]
   [essen.keyboard]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [integrant.core :as ig]
   [cardo.views :as views]
   [cardo.db :as db]

   [cardo.methods]
   [cardo.config :as config]
   [cardo.create]
   [cardo.update]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
    (.getElementById js/document "interface")))

(defn ^:export init []
  (essen.core/init config/config)
  (re-frame/dispatch-sync [::initialize-db])
  (dev-setup)
  (mount-root)
  (essen.keyboard/disable-tabbing!)
  (essen.keyboard/body-event-listener "keydown" es/emit-keydown!)
  (essen.keyboard/body-event-listener "keyup" es/emit-keyup!))

(defn stop []
  (essen.core/suspend!))

(defn start []
  (essen.core/resume config/config))
