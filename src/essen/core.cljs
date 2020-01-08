(ns essen.core
  (:require [integrant.core :as ig]
            [essen.scene]
            [essen.module]
            [phaser]
            [essen.state :refer [system phaser-game phaser-scenes]]))

(defmethod ig/init-key :essen/core [_ {:essen/keys [game] :as opts}]
  (reset! phaser-game (js/Phaser.Game. (clj->js game)))
  opts)

(defmethod ig/halt-key! :essen/core [_ opts]
  (when @phaser-game
    (.destroy @phaser-game)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/suspend-key! :essen/core [_ opts]
  (when @phaser-game
    (.destroy @phaser-game)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/resume-key :essen/core [_ {:essen/keys [game] :as opts} old-opts old-impl]
  (reset! phaser-game (js/Phaser.Game. (clj->js game))))

(defmethod ig/init-key :essen/scenes [_ opts]
  opts)

(defn init [config]
  (->> config
       (ig/prep)
       (ig/init)
       (reset! system)))

(defn suspend! []
  (ig/suspend! @system))

(defn resume [config]
  (reset! system (-> config
                     (ig/prep)
                     (ig/resume @system))))
