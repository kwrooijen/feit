(ns cardo.core
  (:require
   [essen.core]
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

(defn body-event-listener [trigger what-do]
  (.addEventListener (js/document.querySelector "body") trigger
                     (fn [event]
                       (when (= (.-body js/document)
                                (.-activeElement js/document))
                         (what-do event)))))

(defn disable-tabbing
  "This disables the tabbing of HTML elements. We need this to ensure that the
  user can't accidentally tab focus away from the body. Otherwise we can't
  listen to key events. We listen only to the body so that input keys don't
  trigger events."
  []
  (let [tab-key 9]
    (set! (.-onkeydown js/document)
          #(not= tab-key (.-which %)))))

(defn ^:export init []
  (essen.core/init config/config)
  (re-frame/dispatch-sync [::initialize-db])
  (dev-setup)
  (mount-root)
  (disable-tabbing)
  (body-event-listener "keydown" println))
(defn stop []
  (essen.core/suspend!))

(defn start []
  (essen.core/resume config/config))
