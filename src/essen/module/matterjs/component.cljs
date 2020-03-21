(ns essen.module.matterjs.component
  (:require
   ["matter-js" :as Matter :refer [Bodies]]
   [essen.module.matterjs.world :as matterjs.world]
   [essen.util :refer [keep-ns top-key]]
   [integrant.core :as ig]))

(defn- add-label [opts k]
  (merge {:component.opts/label k} opts))

(defn- body-opts  [opts k]
  (-> opts
      (keep-ns :component.opts)
      (add-label (str k))
      (clj->js)))

;; TODO: Create a custom print method so that when we print the object, we
;; don't cause an infilite loop.
;; (extend-protocol IPrintWithWriter
;;   Body
;;   (-pr-writer [new-obj writer _]
;;     (write-all writer "#myObj \"" (:details new-obj) "\"")))

(defmethod ig/init-key :matterjs.component/rectangle
  [k {:component/keys [x y width height] :as opts}]
  (let [body (.rectangle Bodies x y width height (body-opts opts (top-key k)))]
    (matterjs.world/add! body)
    {:component/body (fn [] body)}))

(defmethod ig/suspend-key! :matterjs.component/rectangle
  [_ {:component/keys [state]}]
  (matterjs.world/remove! ((:component/body state))))

(defmethod ig/halt-key! :matterjs.component/rectangle
  [_ {:component/keys [state]}]
  (matterjs.world/remove! ((:component/body state))))

(defmethod ig/init-key :matterjs.component/circle
  [k {:component/keys [x y radius] :as opts}]
  (let [body (.circle Bodies x y radius (body-opts opts (top-key k)))]
    (matterjs.world/add! body)
    {:component/body (fn [] body)}))

(defmethod ig/suspend-key! :matterjs.component/circle
  [_ {:component/keys [state]}]
  (matterjs.world/remove! ((:component/body state))))

(defmethod ig/halt-key! :matterjs.component/circle
  [_ {:component/keys [state]}]
  (matterjs.world/remove! ((:component/body state))))

(def config
  {[:essen/component :matterjs.component/rectangle] {}
   [:essen/component :matterjs.component/circle] {}})
