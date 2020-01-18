(ns cardo.core
  (:require
   [essen.core]
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [integrant.core :as ig]
   [cardo.views :as views]
   [cardo.db :as db]
   [cardo.config :as config]))

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
  (mount-root))

(defn stop []
  (essen.core/suspend!))

(defn start []
  (essen.core/resume config/config))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn set-bg [obj x y flip-x flip-y]
  (.. obj
      (image x y "bg")
      (setOrigin 0)
      (setFlipX flip-x)
      (setFlipY flip-y)))

(defn create-anim [anims k prefix end repeat framerate]
  (println "Creating " k)
  (let [frames (.generateFrameNames anims "atlas" #js {:prefix prefix
                                                       :end end
                                                       :zeroPad 2})]
    (.create anims #js {:key k
                        :frames frames
                        :frameRate framerate
                        :repeat repeat})))

(essen.core/custom-methods!
 {[:set-bg 5] set-bg
  [:create-anim 6] create-anim})

(defmethod ig/init-key :my/updater [_ opts]
  (fn [{:game/keys [cursor adventurer] :as state} delta this]
    (when (.. cursor -space -isDown)
      (set! (.-delay (:attack/timer state)) 600))
    state))

(defmethod ig/init-key :adventurer/timer [_ {:keys [adventurer]}]
  (fn []
    (.play adventurer  "adventurer/attack")
    (.. adventurer -anims (chain "adventurer/idle"))))
