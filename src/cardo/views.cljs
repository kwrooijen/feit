(ns cardo.views
  (:require
   [essen.re-frame :refer [active-scenes]]
   [essen.core :refer [scene-change scene-state]]))

(defn attack []
  (-> (scene-state :scene/battle)
      :game/adventurer
      (.play "adventurer/attack")
      (.. -anims (chain "adventurer/idle"))))

(defn scenes-view []
  [:<>
   (let [scenes (active-scenes)]
     (for [scene scenes]
       [:div scene]))])

(defn main-panel []
  [:div {:style {:height "100%" :width "100%" :position :relative}}
   [:div {:style {:width "100%" :height "40px" :position :relative :bottom 0 :background-color "#aaaaff"}}]
   [scenes-view]
   [:div#attack.button
    {:on-click attack}
    "Attack"]])
