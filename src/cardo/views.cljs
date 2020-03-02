(ns cardo.views)

(defn main-panel []
  [:div
   [:div "Hi Interface"]
   [:div
    {:style {:width "30px"
             :height "30px"
             :position :absolute
             :left "85px"
             :top "85px"
             :background-color :red}}]])
