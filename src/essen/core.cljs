(ns essen.core
  (:require [integrant.core :as ig]
            [essen.scene]
            [essen.module]))

(defonce system (atom nil))
(defonce game-phaser (atom nil))
(defonce game-config (atom nil))

(defmethod ig/init-key :essen/core [_ opts]
  (reset! game-config opts)
  (reset! game-phaser (js/Phaser.Game. (clj->js opts))))

(defmethod ig/halt-key! :essen/core [_ opts]
  (when @game-config
    (.destroy @game-phaser)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/suspend-key! :essen/core [_ opts]
  (when @game-config
    (.destroy @game-phaser)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/resume-key :essen/core [_ opts old-opts old-impl]
  (reset! game-config opts)
  (reset! game-phaser (js/Phaser.Game. (clj->js opts))))

(defn init [config]
  (reset! system (ig/init config)))

(defn suspend! []
  (ig/suspend! @system))

(defn resume [config]
  (reset! system (ig/resume config @system)))
