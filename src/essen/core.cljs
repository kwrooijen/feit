(ns essen.core
  (:require [integrant.core :as ig]
            [essen.scene]
            [essen.module]
            [essen.state :refer [system phaser-game phaser-scenes]]))

(defn new-scene [k]
  #js {:key (name k)
       :preload #(this-as this (swap! phaser-scenes assoc k this))})

(defn add-scene [game scene]
  (doto
      (.-scene game)
      (.add (.-key scene) scene true)
      (.getScene (.-key scene))))

(defn wait-for-scenes [scenes callback]
  (if (= (count scenes) (count (keys @phaser-scenes)))
    (callback)
    (js/setTimeout #(wait-for-scenes scenes callback)
                   10)))

(defmethod ig/init-key :essen/core [_ {:essen/keys [game ] :as opts}]
  (reset! phaser-game (js/Phaser.Game. (clj->js game)))
  ;; (doseq [scene scenes]
  ;;   (add-scene @phaser-game (new-scene scene)))
  opts)


(defmethod ig/halt-key! :essen/core [_ opts]
  (when @phaser-game
    (.destroy @phaser-game)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/suspend-key! :essen/core [_ opts]
  (when @phaser-game
    (.destroy @phaser-game)
    (.remove (js/document.querySelector "canvas"))))

(defmethod ig/resume-key :essen/core [_ opts old-opts old-impl]
  (reset! phaser-game (js/Phaser.Game. (clj->js opts))))

(defmethod ig/init-key :essen/scenes [_ opts]
  opts)

(defn init [config]
  (let [init-system (ig/init config [:essen/core])
        scenes (-> init-system (:essen/core) (:essen/scenes))]
    (wait-for-scenes scenes
     (fn []
       (-> config
           (ig/prep [(-> init-system (:essen/core) (:essen/initial-scene))])
           (ig/init [(-> init-system (:essen/core) (:essen/initial-scene))]))))))

(defn suspend! []
  (ig/suspend! @system))

(defn resume [config]
  (reset! system (ig/resume config @system)))
