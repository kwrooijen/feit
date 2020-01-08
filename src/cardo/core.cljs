(ns cardo.core
  (:require [cardo.config :refer [config]]
            [integrant.core :as ig]
            [essen.core]))

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
