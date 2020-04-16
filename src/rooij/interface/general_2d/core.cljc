(ns rooij.interface.general-2d.core
  (:require
   [rooij.query :refer [emit!]]
   [integrant-tools.core :as it]
   [integrant-tools.keyword :refer [descendant?]]
   [integrant.core :as ig]
   [rooij.config]))

(defprotocol RooijGeneral2DPosition
  (set-position [this x y])
  (get-position [this]))

(defn positionable? [k]
  (descendant? k :rooij/position))

(defmethod ig/init-key :general-2d.ticker.position/emitter [_ opts]
  (let [last-position (atom {:x 0 :y 0})]
    (fn [{:context/keys [scene-key entity-key]} state]
      (let [new-position (get-position state)]
        ;; TODO sometimes matterjs twitches while idle. Maybe put a 1 pixel margin for difference?
        (when (not= @last-position new-position)
          (reset! last-position new-position)
          (emit!
           {:event/scene scene-key
            :event/entity entity-key
            :event/handler :general-2d.handler.position/set
            :event/content new-position}))))))

(defmethod ig/init-key :general-2d.handler.position/set [_ opts]
  (fn [_context {:keys [x y]} state]
    (set-position state x y)))

(it/derive-hierarchy
 {:general-2d.handler.position/set [:rooij/handler]
  :general-2d.ticker.position/emitter [:rooij/ticker]})

(rooij.config/merge-interface!
 {:general-2d.handler.position/set {}
  :general-2d.ticker.position/emitter {}})
