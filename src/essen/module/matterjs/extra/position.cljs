(ns essen.module.matterjs.extra.position
  (:require
   [essen.core :as essen]
   ["matter-js" :as Matter :refer [Body]]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [integrant.core :as ig]))

;; TODO add a ticker to update position
;; Event should have an exlude key, where you can exclude components
;; We don't want to cause an infinite loop
(defmethod ig/init-key :handler.essen.position.matterjs/set [_ _opts]
  (fn [_context {:position/keys [x y]} state]
    (.setPosition Body (:body state)
                  #js {:x (+ x (:x state))
                       :y (+ y (:y state))})
    state))

(defmethod ig/init-key :ticker.essen.position.matterjs/update [k opts]
  (fn [{:context/keys [scene-key entity-key]} {:keys [body x y]}]
    (essen/emit!
     {:event/scene scene-key
      :event/entity entity-key
      :event/handler :handler.essen.position/set
      :event/excludes [:matterjs/body]
      :event/content
      {:position/x (- (.. body -position -x) x)
       :position/y (- (.. body -position -y) y)}})))

(def handlers [(ig/ref :handler.essen.position.matterjs/set)])

(def tickers [(ig/ref :ticker.essen.position.matterjs/update)])

(def config
  {[:essen/handler :handler.essen.position.matterjs/set]
   {:handler/route :handler.essen.position/set}

   [:essen/ticker :ticker.essen.position.matterjs/update] {}})
