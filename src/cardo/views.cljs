(ns cardo.views
  (:require
   [essen.core :refer [emit! scenes stop-scene start-scene entity entities]]))

(defn attack! [entity damage]
  (emit! :scene/battle entity :handler.stats/attack {:event/damage damage}))

(defn view-start []
  [:div {:style {:color :white}}
   [:div "Starting..."]
   [:div (str (entity :scene/start :entity/player))]
   [:div {:on-click #(do (stop-scene :scene/start)
                         (start-scene :scene/battle {:enemies 10}))}
    "To battle!"]])

(defn view-battle []
  [:div {:style {:color :white}}
   [:div "Battle!"]
   [:div (str (entities :scene/battle :essen/entity))]
   [:div {:on-click  #(do (stop-scene :scene/battle)
                          (start-scene :scene/start))}
    "Retreat!"]
   [:div
    {:on-click #(attack! :entity/player 3)
     :style {:width "30px"
             :height "30px"
             :position :absolute
             :left "85px"
             :top "85px"
             :background-color :red}}]])

(defn main-panel []
  [:div
   (cond
     ((scenes) :scene/start) [view-start]
     ((scenes) :scene/battle) [view-battle])])
