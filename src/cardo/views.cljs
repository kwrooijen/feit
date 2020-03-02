(ns cardo.views
  (:require
   [essen.core :refer [emit!]]))

(defn attack! [entity damage]
  (emit! :scene/start entity :handler.stats/attack {:event/damage damage}))

(defn main-panel []
  [:div
   [:div "Hi Interface"]
   [:div
    {:on-click #(attack! :entity/player 3)
     :style {:width "30px"
             :height "30px"
             :position :absolute
             :left "85px"
             :top "85px"
             :background-color :red}}]])
