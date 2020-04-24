(ns rooij.interface.general-2d.position
  (:require
   [rooij.query :refer [emit!]]
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [rooij.config]))

(defprotocol RooijGeneral2DPosition
  (set-position [this x y angle])
  (get-position [this]))

(defn- position-changed?
  "Predicate to check if the position has changed compared to the previous
  position. We leave a 1 pixel margin to since physics engines sometimes twitch.
  Causing them to constantly change by 1px."
  [old-position new-position]
  (or (not (#{-1 0 1} (- (:x old-position) (:x new-position))))
      (not (#{-1 0 1} (- (:y old-position) (:y new-position))))
      (not (#{-1 0 1} (- (:angle old-position) (int (* 100 (:angle new-position))))))))

(defn- update-old-position! [old-position new-position]
  (reset! old-position (update new-position :angle (comp int #(* 100 %)))))

(defmethod ig/init-key :general-2d.ticker.position/emitter [_ _opts]
  (let [old-position (atom {:x 0 :y 0 :angle 0})]
    (fn [{:context/keys [scene-key entity-key component-key]} state]
      (let [new-position (get-position state)]
        (when (position-changed? @old-position new-position)
          (update-old-position! old-position new-position)
          (emit!
           {:event/scene scene-key
            :event/entity entity-key
            :event/excludes [component-key]
            :event/handler :general-2d.handler.position/set
            :event/content new-position}))))))

(defmethod ig/init-key :general-2d.handler.position/set [_ opts]
  (fn [_context {:keys [x y angle]} state]
    (set-position state x y angle)))

(it/derive-hierarchy
 {:general-2d.handler.position/set [:rooij/handler]
  :general-2d.ticker.position/emitter [:rooij/ticker]})

(rooij.config/merge-interface!
 {:general-2d.handler.position/set {}
  :general-2d.ticker.position/emitter {}})
