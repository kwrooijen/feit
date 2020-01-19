(ns essen.events.scene
  (:require
   [essen.state :refer [phaser-game scenes]]
   [re-frame.core :as re-frame]
   [clojure.spec.alpha :as s]
   [spec-signature.core :refer-macros [sdef]]))

(def active-scenes-xf
  (comp (filter #(.. % isActive))
        (map #(.. % -key))))

(sdef active-scenes [] (s/coll-of :scene/key))
(defn active-scenes []
  (transduce active-scenes-xf conj (scenes)))

(re-frame/reg-event-db
 ::set-active-scenes
 (fn [db [_]]
   (assoc-in db [:essen :active-scenes] (active-scenes))))
