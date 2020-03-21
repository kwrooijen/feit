(ns cardo.component.position
   (:require
    [clojure.spec.alpha :as s]
    [integrant.core :as ig]) )

(defmethod ig/pre-init-spec :component/position [_]
  (s/keys :req [:position/x
                :position/y]))

(defmethod ig/init-key :component/position [_ {:position/keys [x y]}]
  {:position/x x
   :position/y y})

(defmethod ig/init-key :handler.position/move [_ _opts]
  (fn [_context {:event/keys [x y] :as _event} state]
    (-> state
        (update :position/x + x)
        (update :position/y + y))))

(defmethod ig/init-key :handler.position/set [_ _opts]
  (fn [_context {:event/keys [x y] :as _event} state]
    (-> state
        (assoc :position/x x
               :position/y y))))

(def config
  {[:essen/component :component/position]
   {:component/handlers [(ig/ref :handler.position/move)
                         (ig/ref :handler.position/set)]}
   [:essen/handler :handler.position/move] {}
   [:essen/handler :handler.position/set] {}})
