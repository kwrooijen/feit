(ns cardo.update.battle
  (:require
   [integrant.core :as ig]))

(defmethod ig/init-key :my/updater [_ opts]
  (fn [{:game/keys [cursor adventurer] :as state} this time delta]
    (when (.. cursor -space -isDown)
      (.. this -scene (start "battle" {:FUCK 1}))
      (set! (.-delay (:attack/timer state)) 600))
    state))
