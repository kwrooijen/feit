(ns cardo.views
  (:require
   [essen.re-frame :refer [active-scenes]]
   [essen.core :refer [scene-change scene-state]]))

(defn attack []
  (-> (scene-state :scene/battle)
      :game/adventurer
      (.play "adventurer/attack")
      (.. -anims (chain "adventurer/idle"))))

(defn view-boot []
  [:div "Boot"])

(defn view-town []
  [:div
   [:div.button
    {:on-click #(scene-change :scene/town :scene/battle {})}
    "To battle"]])

(defn view-battle []
  [:div#attack.button
   {:on-click attack}
   "Attack"])

(defn main-panel []
  (let [scene-active? (set (active-scenes))]
    (cond
      (scene-active? "boot")   [view-boot]
      (scene-active? "town")   [view-town]
      (scene-active? "battle") [view-battle]
      :else [:div "Loading"])))
