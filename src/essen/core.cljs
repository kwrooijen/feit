(ns essen.core
  (:require
   [integrant.core :as ig]
   [essen.scene]
   [essen.obj]
   [phaser]
   [essen.spec.scene]
   [clojure.spec.alpha :as s]
   [spec-signature.core :refer-macros [sdef]]))

(defonce system (atom nil))
(defonce phaser-game (atom nil))

(def active-scenes-xf
  (comp (filter #(.. % isActive))
        (map #(.. % -key))))

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

(sdef scenes [] (s/coll-of object?))
(defn scenes []
  (if @phaser-game
    (mapv #(.-scene %) (.. @phaser-game -scene -scenes))
    []))

(sdef scene-keys [] (s/coll-of :scene/key))
(defn scene-keys []
  (mapv #(.-key %) (scenes)))

(sdef active-scenes [] (s/coll-of :scene/key))
(defn active-scenes []
  (transduce active-scenes-xf conj (scenes)))
