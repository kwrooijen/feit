(ns essen.module.matterjs.component.rectangle
  (:require
   ["matter-js" :as Matter :refer [Bodies]]
   [essen.module.matterjs.world :as matterjs.world]
   [integrant.core :as ig]))

(defmethod ig/init-key :matterjs.component/rectangle
  [_ {:component/keys [x y width height opts]}]
  (let [body (.rectangle Bodies x y width height (clj->js opts))]
    (matterjs.world/add body)
    {:component/body (fn [] body)}))

(defmethod ig/resume-key :matterjs.component/rectangle [key opts old-opts old-impl]
  (println "RESUMING")
  opts)

(defmethod ig/suspend-key! :matterjs.component/rectangle
  [_ {:component/keys [state]}]
  (matterjs.world/remove ((:component/body state))))

(derive :matterjs.component/rectangle :essen/component)
