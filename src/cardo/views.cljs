(ns cardo.views
  (:require
   [essen.re-frame :refer [active-scenes]]
   [essen.core :refer [scene-change emit!]]))

(defn view-boot []
  [:div "Boot"])

(defn view-town []
  [:div
   [:div.button
    {:on-click #(scene-change :scene/town :scene/battle {})}
    "To battle"]])

(defn view-battle []
  [:div
   [:input {:type :text}]
   [:div#attack.button
    {:on-click #(emit! :scene/battle {:event :attack})}
    "Attack"]])

(defn main-panel []
  (let [scene-active? (set (active-scenes))]
    (cond
      (scene-active? "boot")   [view-boot]
      (scene-active? "town")   [view-town]
      (scene-active? "battle") [view-battle]
      :else [view-boot])))
