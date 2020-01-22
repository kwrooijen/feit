(ns essen.core
  (:require [phaser])
  (:require
   [clojure.spec.alpha :as s]
   [essen.events.scene]
   [essen.keyboard]
   [essen.obj]
   [essen.scene]
   [essen.spec.scene]
   [essen.state :refer [system phaser-game scene-queues scenes]]
   [essen.subs.scene]
   [integrant.core :as ig]
   [spec-signature.core :refer-macros [sdef]]))

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

(defn emit!
  "Emit an event to a specific scene, or all scenes.
  Events are pushed into a queue (atom vector) and are all handled in a single
  update rotation. After the update rotation, all events in the queue are removed.

  TODO: If globally emitted events (e.g. keyboard event) are also pushed to non
  active scenes (scenes which currently are running an update loop). This is
  problematic because that means the queue for these sceness will keep growing.
  "
  ([event]
   (swap! scene-queues
          #(reduce-kv (fn [m k v] (assoc m k (conj v event))) {} %)))
  ([scene-key event]
   (swap! scene-queues
          #(update % scene-key conj event) )))

(defn emit-keydown!
  "A standard format for emitting keydown events. This is to keep keyboard
  events consistent between libraries"
  [event]
  (emit! {:event/key-down (essen.keyboard/get-key event)}))

(defn emit-keyup!
  "A standard format for emitting keyup events. This is to keep keyboard
  events consistent between libraries"
  [event]
  (emit! {:event/key-up (essen.keyboard/get-key event)}))
