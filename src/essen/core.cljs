(ns essen.core
  (:require
   [integrant.core :as ig]
   [essen.scene]
   [essen.obj]
   [phaser]))

(defonce system (atom nil))
(defonce phaser-game (atom nil))

(defn custom-methods! [methods]
  (swap! essen.obj/custom-methods merge methods))

(defmethod ig/init-key :essen/game [_ opts]
  (reset! phaser-game (js/Phaser.Game. (clj->js opts)))
  opts)

(defmethod ig/halt-key! :essen/game [_ _]
  (when @phaser-game
    (.destroy @phaser-game)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/suspend-key! :essen/game [_ _]
  (when @phaser-game
    (.destroy @phaser-game)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/resume-key :essen/game
  [_ opts _old-opts _old-impl]
  (reset! phaser-game (js/Phaser.Game. (clj->js opts)))
  opts)

(defmethod ig/init-key :essen/const [_ opts]
  opts)

(defn init [config]
  (-> config
       (ig/prep)
       (ig/init [:essen/game])
       (->> (reset! system))))

(defn suspend! []
  (ig/suspend! @system))

(defn resume [config]
  (reset! system
          (-> config
              (ig/prep)
              (ig/resume @system [:essen/game]))))
