(ns essen.module.matterjs
  (:require
   ["matter-js" :as MatterJs :refer [Engine Render Bodies World Mouse MouseConstraint Composite]]
   [integrant.core :as ig]))

;; Remove a body
;; (.remove Composite (.-world engine) boxA)
(defonce box (atom nil))

(defonce engine1 (atom nil))
(defonce render1 (atom nil))

(defn run []
  (.update Engine @engine1 (/ 1000 60) 1))

(defmethod ig/init-key :matterjs/start [_ opts]
  (println "Creating engineeeee")
  (let [engine (.create Engine (clj->js {:positionIterations 200
                                         :velocityIterations 200}))
        render (.create Render (clj->js {:element (.getElementById js/document "app")
                                         :engine engine}))
        boxA (.rectangle (.-Bodies MatterJs) 400 200 20 31 #js {:restitution 1})

        ground (.rectangle Bodies 400 610 810 60 (clj->js {:isStatic true
                                                           :restitution 1
                                                           :render {:fillStyle :red
                                                                    :strokeStyle :blue
                                                                    :lineWidth 3}}))
        mouse (.create Mouse  (.getElementById js/document "game"))
        mouseConstraint  (.create MouseConstraint engine
                                  (js->clj {:mouse mouse
                                            :constraint
                                            {:stiffness 0}}))]
    (reset! box boxA)
    (println (.-velocityIterations engine))
    (.add World (.-world engine) #js [boxA ground])
    (.add World (.-world engine) mouseConstraint)
    (reset! engine1 engine)
    (reset! render1 render)
    ;; For debugging
    (.run Render render)))
