(ns essen.module.matterjs.component
  (:require
   ["matter-js" :as Matter :refer [Bodies]]
   [essen.module.matterjs.shape.handler :as shape.handler]
   [com.rpl.specter :as specter :refer [MAP-VALS] :refer-macros [transform]]
   [meta-merge.core :refer [meta-merge]]
   [essen.module.matterjs.world :as matterjs.world]
   [essen.util :refer [keep-ns top-key ns-map->nested-map]]
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

(defn- rectangle? [shape]
  (some? (:rectangle/x shape)))

(defn- circle? [shape]
  (some? (:circle/x shape)))

(defn- shape->body-opts [shape]
  (-> shape
      (ns-map->nested-map)
      (get :body)
      (clj->js)))

(defn- opts->shapes [{:component/keys [shapes] :as opts}]
  (transform [MAP-VALS]
             (partial meta-merge (dissoc opts :component/shapes))
             shapes))

(defn- create-rectangle [{:rectangle/keys [x y width height] :as shape}]
  (let [body (.rectangle Bodies x y width height (shape->body-opts shape))]
    (matterjs.world/add! body)
    {:shape/body (fn [] body)}))

(defn- create-circle [{:circle/keys [x y radius] :as shape}]
  (let [body (.circle Bodies x y radius (shape->body-opts shape))]
    (matterjs.world/add! body)
    {:shape/body (fn [] body)}))

;; TODO Is shapes correct? Should this maybe be "bodies" ? This includes the
;; handlers / middleware
(defmethod ig/init-key :matterjs.component/shapes [_ opts]
  (fn [_context]
    {:component/shapes
     (into {}
           (for [[k v] (opts->shapes opts)]
             [k (cond
                  (rectangle? v) (create-rectangle v)
                  (circle? v) (create-circle v))]))}))

(defmethod ig/suspend-key! :matterjs.component/shapes
  [_ {:component/keys [state persistent]}]
  (when-not persistent
    (doseq [[_ v] (:component/shapes state)]
      (matterjs.world/remove! ((:shape/body v))))))

(defmethod ig/halt-key! :matterjs.component/shapes [_ _opts]
  (fn [{:component/keys [state]}]
    (doseq [[_ v] (:component/shapes state)]
      (matterjs.world/remove! ((:shape/body v))))))

(defmethod ig/init-key :matterjs.component/rectangle
  [k {:component/keys [x y width height] :as opts}]
  (fn [_context]
    (let [body (.rectangle Bodies x y width height (body-opts opts (top-key k)))]
      (matterjs.world/add! body)
      {:component/body (fn [] body)})))

(defmethod ig/suspend-key! :matterjs.component/rectangle
  [_ {:component/keys [state persistent]}]
  (when-not persistent
    (matterjs.world/remove! ((:component/body state)))))

(defmethod ig/halt-key! :matterjs.component/rectangle
  [_ _opts]
  (fn [{:component/keys [state]}]
    (matterjs.world/remove! ((:component/body state)))))

(defmethod ig/init-key :matterjs.component/circle
  [k {:component/keys [x y radius] :as opts}]
  (fn [_context]
    (let [body (.circle Bodies x y radius (body-opts opts (top-key k)))]
      (matterjs.world/add! body)
      {:component/body (fn [] body)})))

(defmethod ig/suspend-key! :matterjs.component/circle
  [_ {:component/keys [state persistent]}]
  (when-not persistent
    (matterjs.world/remove! ((:component/body state)))))

(defmethod ig/halt-key! :matterjs.component/circle
  [_ _opts]
  (fn [{:component/keys [state]}]
    (matterjs.world/remove! ((:component/body state)))))

(def config
  (merge
   shape.handler/config
   {[:essen/component :matterjs.component/shapes]
    {:component/handlers shape.handler/handlers}

    [:essen/component :matterjs.component/rectangle] {}
    [:essen/component :matterjs.component/circle] {}}))
