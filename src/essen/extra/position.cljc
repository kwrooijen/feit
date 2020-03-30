(ns essen.extra.position
  (:require
   [essen.core :as essen]
   [integrant.core :as ig]))

(def required
  (comment
    :interface.essen/position
    ;; -- handler
    :interface.essen.position/set
    {:position/x 10
     :position/y 10
     :position/excludes [:graphics]}))

(defmethod ig/init-key :component.essen/position [_ {:position/keys [x y]}]
  ;; TODO separate context + rename scene / entity to scene-key and entity-key
  (fn [{:context/keys [scene entity]}]
    (essen/emit!
     {:event/scene scene
      :event/entity entity
      :event/handler :handler.essen.position/set
      :event/content {:position/x x :position/y y}})
    {:position/x x
     :position/y y}))

(defmethod ig/init-key :handler.essen.position/set [_ opts]
  (fn [context event state]
    (println "CONTEXT " context)
    state))


(def config
  {[:essen/component :component.essen/position] {}})
