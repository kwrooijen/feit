(ns rooij.module.matterjs.extra.position
  (:require
   [rooij.core :as rooij]
   ["matter-js" :as Matter :refer [Body]]
   [integrant.core :as ig]))

;; TODO add a ticker to update position
;; Event should have an exlude key, where you can exclude components
;; We don't want to cause an infinite loop
(defmethod ig/init-key :handler.rooij.position.matterjs/set [_ _opts]
  (fn [_context {:position/keys [x y]} state]
    (.setPosition Body (:body state)
                  #js {:x (+ x (:x state))
                       :y (+ y (:y state))})
    state))

(defmethod ig/init-key :ticker.rooij.position.matterjs/update [k opts]
  (fn [{:context/keys [scene-key entity-key]} {:keys [body x y]}]
    (rooij/emit!
     {:event/scene scene-key
      :event/entity entity-key
      :event/handler :handler.rooij.position/set
      :event/excludes [:matterjs/body]
      :event/content
      {:position/x (- (.. body -position -x) x)
       :position/y (- (.. body -position -y) y)}})))

(def handlers [(ig/ref :handler.rooij.position.matterjs/set)])

(def tickers [(ig/ref :ticker.rooij.position.matterjs/update)])

(def config
  {[:rooij/handler :handler.rooij.position.matterjs/set]
   {:handler/route :handler.rooij.position/set}

   [:rooij/ticker :ticker.rooij.position.matterjs/update] {}})
