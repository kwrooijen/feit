(ns essen.core
  (:require
   [phaser]
   [essen.events.scene]
   [essen.subs.scene]
   [integrant.core :as ig]
   [essen.scene]
   [essen.obj]
   [essen.spec.scene]
   [clojure.spec.alpha :as s]
   [spec-signature.core :refer-macros [sdef]]
   [essen.state :refer [system phaser-game scene-states scenes]]))

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

(defn scene [scene-key]
  (->> (scenes)
       (filter #(#{scene-key} (.-key %)))
       (first)))

(defn scene-change
  ([scene1 scene2]
   (scene-change scene1 scene2 {}))
  ([scene1 scene2 opts]
   (.start (scene (name scene1)) (name scene2) opts)))

(defn scene-state [scene-key]
  @(get @scene-states scene-key))

(defn emit! [scene-key event]
  (swap! (get @scene-states scene-key)
         #(update % :essen/queue conj event)))
