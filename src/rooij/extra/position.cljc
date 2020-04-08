(ns rooij.extra.position
  (:require
   [rooij.core :as rooij]
   [integrant.core :as ig]))

(defmethod ig/init-key :component.rooij/position [_ {:position/keys [x y]}]
  ;; TODO separate context + rename scene / entity to scene-key and entity-key
  (fn [{:context/keys [scene-key entity-key]}]
    (rooij/emit!
     {:event/scene scene-key
      :event/entity entity-key
      :event/handler :handler.rooij.position/set
      :event/content {:position/x x :position/y y}})
    {:position/x x
     :position/y y}))

(defmethod ig/init-key :handler.rooij.position/set [_ _opts]
  (fn [_context event _state]
    (select-keys event [:position/x :position/y])))

(def config
  {[:rooij/component :component.rooij/position]
   {:component/handlers [(ig/ref :handler.rooij.position/set)]}

   [:rooij/handler :handler.rooij.position/set] {}})
