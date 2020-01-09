(ns essen.core
  (:require
   [integrant.core :as ig]
   [essen.scene]
   [phaser]))

(defonce system (atom nil))
(defonce phaser-game (atom nil))

(defn custom-methods! [methods]
  (swap! essen.scene/method-collection merge methods))

(defmethod ig/init-key :essen/core [_ {:essen/keys [game] :as opts}]
  (reset! phaser-game (js/Phaser.Game. (clj->js game)))
  opts)

(defmethod ig/halt-key! :essen/core [_ _]
  (when @phaser-game
    (.destroy @phaser-game)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/suspend-key! :essen/core [_ _]
  (when @phaser-game
    (.destroy @phaser-game)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/resume-key :essen/core
  [_ {:essen/keys [game] :as opts} _old-opts _old-impl]
  (reset! phaser-game (js/Phaser.Game. (clj->js game)))
  opts)

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
  (reset! system
          (-> config
              (ig/prep)
              (ig/resume @system))))
