(ns essen.module.matterjs.component
  (:require
   [essen.system.component :as component]
   [essen.module.matterjs.extra.position :as extra.position]
   ["matter-js" :as Matter :refer [Bodies]]
   [essen.module.matterjs.shape.handler :as shape.handler]
   [essen.module.matterjs.world :as matterjs.world]
   [essen.util :refer [ns-map->nested-map]]
   [integrant.core :as ig]))

(derive :matterjs.component/rectangle :matterjs/body)
(derive :matterjs.component/circle :matterjs/body)

(defrecord MatterjsBody [body x y])

(extend-protocol IPrintWithWriter
  MatterjsBody
  (-pr-writer [new-obj writer _]
    (write-all writer "#MatterjsBody")))

(defn- shape->body-opts [shape]
  (-> shape
      (ns-map->nested-map)
      (get :body)
      (clj->js)))

(defmethod ig/init-key :matterjs.component/rectangle
  [_ {:rectangle/keys [x y width height] :as opts}]
  (let [body (.rectangle Bodies x y width height (shape->body-opts opts))]
    (matterjs.world/add! body)
    (map->MatterjsBody. {:body body :x x :y y})))

(defmethod component/persistent-resume :matterjs.component/rectangle [_key _opts state]
  (matterjs.world/add! (:body state))
  state)

(defmethod ig/suspend-key! :matterjs.component/rectangle
  [_ {:component/keys [state persistent]}]
  (when-not persistent
    (matterjs.world/remove! (:body state))))

(defmethod ig/halt-key! :matterjs.component/rectangle
  [_ _opts]
  (fn [{:component/keys [state]}]
    (matterjs.world/remove! (:body state))))

(defmethod ig/init-key :matterjs.component/circle
  [_ {:circle/keys [x y radius] :as opts}]
  (let [body (.circle Bodies x y radius (shape->body-opts opts))]
    (matterjs.world/add! body)
    (map->MatterjsBody. {:body body :x x :y y})))

(defmethod component/persistent-resume :matterjs.component/circle [_key _opts state]
  (matterjs.world/add! (:body state))
  state)

(defmethod ig/suspend-key! :matterjs.component/circle
  [_ {:component/keys [state persistent]}]
  (when-not persistent
    (matterjs.world/remove! (:body state))))

(defmethod ig/halt-key! :matterjs.component/circle
  [_ _opts]
  (fn [{:component/keys [state]}]
    (matterjs.world/remove! (:body state))))

(def config
  (merge
   shape.handler/config
   extra.position/config
   {[:essen/component :matterjs.component/rectangle]
    {:component/handlers [shape.handler/handlers
                          extra.position/handlers]
     :component/tickers [extra.position/tickers]}

    [:essen/component :matterjs.component/circle]
    {:component/handlers [shape.handler/handlers
                          extra.position/handlers]
     :component/tickers [extra.position/tickers]}}))
