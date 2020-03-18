(ns essen.module.matterjs.component
  (:require
   ["matter-js" :as Matter :refer [Bodies]]
   [essen.module.matterjs.world :as matterjs.world]
   [essen.util :refer [keep-ns]]
   [integrant.core :as ig]))

(defn- add-label [opts k]
  (merge {:component.opts/label k} opts))

(defn- body-opts  [opts k]
  (-> opts
      (keep-ns :component.opts)
      (add-label (str k))
      (clj->js)))

(defmethod ig/init-key :matterjs.component/rectangle
  [[_ k] {:component/keys [x y width height] :as opts}]
  (let [body (.rectangle Bodies x y width height (body-opts opts k))]
    (matterjs.world/add! body)
    {:component/body (fn [] body)}))

(defmethod ig/suspend-key! :matterjs.component/rectangle
  [_ {:component/keys [state]}]
  (matterjs.world/remove! ((:component/body state))))

(defmethod ig/init-key :matterjs.component/circle
  [[_ k] {:component/keys [x y radius] :as opts}]
  (let [body (.circle Bodies x y radius (body-opts opts k))]
    (matterjs.world/add! body)
    {:component/body (fn [] body)}))

(defmethod ig/suspend-key! :matterjs.component/circle
  [_ {:component/keys [state]}]
  (matterjs.world/remove! ((:component/body state))))

(derive :matterjs.component/rectangle :essen/component)
(derive :matterjs.component/circle :essen/component)
