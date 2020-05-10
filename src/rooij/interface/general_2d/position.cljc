(ns rooij.interface.general-2d.position
  (:require
   [rooij.api :refer [emit!]]
   [rooij.dsl :as r]
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

(defmethod ig/init-key :general-2d.ticker.position/emitter
  [_ {:context/keys [scene-key entity-key component-key]}]
  (let [old-position (atom {:x 0 :y 0 :angle 0})]
    (fn [_context state]
      (let [new-position (get-position state)]
        (when (position-changed? @old-position new-position)
          (update-old-position! old-position new-position)
          (emit!
           {:context/scene-key scene-key
            :context/entity-key entity-key}
           :general-2d.handler.position/set
           new-position
           [component-key]))))))

(defmethod ig/init-key :general-2d.handler.position/set [_ opts]
  (fn [_context {:keys [x y angle]} state]
    (set-position state
                  (or x (:x state))
                  (or y (:y state))
                  (or angle (:angle state)))))

(-> (r/handler :general-2d.handler.position/set)
    (r/ticker :general-2d.ticker.position/emitter)
    (r/save-interface!))
