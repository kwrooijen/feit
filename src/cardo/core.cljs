(ns cardo.core
  (:require [cardo.config :refer [config]]
            [integrant.core :as ig]
            [essen.core]))

(defn set-bg [obj x y flip-x flip-y]
  (.. obj
      (image x y "bg")
      (setOrigin 0)
      (setFlipX flip-x)
      (setFlipY flip-y)))

(essen.core/custom-methods!
 {[:set-bg 5] set-bg})

(defmethod ig/init-key :my/updater [_ opts]
  (fn [{:game/keys [cursor player] :as state} delta this]
    (.setVelocity player 0)
    (when (.. cursor -left -isDown)
      (.setVelocityX player -500))
    (when (.. cursor -right -isDown)
      (.setVelocityX player 500))
    (when (.. cursor -up -isDown)
      (.setVelocityY player -500))
    (when (.. cursor -down -isDown)
      (.setVelocityY player 500))
    state))

(defn ^:export init []
  (essen.core/init config))

(defn stop []
  (essen.core/suspend!))

(defn start []
  (essen.core/resume config))
