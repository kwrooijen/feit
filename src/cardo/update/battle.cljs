(ns cardo.update.battle
  (:require
   [integrant.core :as ig]))

(defn attack-event? [queue]
  (some (comp #{:attack} :event) queue))

(defn attack-threshold? [{:game/keys [last-attack]} time]
  (> (- time last-attack) 1000))

(defn can-attack? [{:essen/keys [queue] :as state} time]
  (and (attack-event? queue)
       (attack-threshold? state time)))

(defn attack [{:game/keys [adventurer] :as state} time]
  (-> adventurer
      (.play "adventurer/attack")
      (.. -anims (chain "adventurer/idle")))
  (assoc state :game/last-attack time))

(defmethod ig/init-key :handle/attack [_ _opts]
  (fn [state this time delta]
    (if (can-attack? state time)
      (attack state time)
      state)))
