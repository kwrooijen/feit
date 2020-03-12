(ns cardo.component.position
   (:require
    [clojure.spec.alpha :as s]
    [meta-merge.core :refer [meta-merge]]
    [integrant.core :as ig]) )

(defmethod ig/prep-key :component/position [_ opts]
  (meta-merge
   opts
   {:component/handlers [(ig/ref :handler.position/move)]
    :component/reactors []
    :component/tickers  []}))

(defmethod ig/pre-init-spec :component/position [_]
  (s/keys :req [:position/x
                :position/y]))

(defmethod ig/init-key :component/position [_ {:position/keys [x y]}]
  {:position/x x
   :position/y y})

;; TODO What if we want to adjust movement speed for a specific entity? How do
;; we do that?
;; NOTE: Maybe we can separate the init-key for component, and the init key for state?
;; The init key for component will setup the arguments for hanlers / reactors etc
;; the init key for state will setup the state. Or somehow combine them?
(defmethod ig/init-key :handler.position/move [_ _opts]
  (fn [_context {:event/keys [x y] :as _event} state]
    (-> state
        (update :position/x + x)
        (update :position/y + y))))

(derive :component/position :essen/component)

(def config
  {[:essen/handler :handler.position/move] {}})
