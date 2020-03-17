(ns essen.module.matterjs.component.rectangle
  (:require
   ["matter-js" :as Matter :refer [Bodies World]]
   [essen.module.matterjs.state :as state]
   [integrant.core :as ig]))

(defmethod ig/init-key :matterjs.component/rectangle
  [_ {:component/keys [x y w h opts]}]
  {:component/body (.rectangle Bodies x y w h (clj->js opts))})

(defmethod ig/suspend-key! :matterjs.component/rectangle
  [_ {:component/keys [body]}]
  (.remove World (.-world @state/engine) body))

(derive :matterjs.component/rectangle :essen/component)
