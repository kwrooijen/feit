(ns essen.module.matterjs.shape.handler
  (:require
   ["matter-js" :as Matter :refer [Body]]
   [integrant.core :as ig]
   [integrant-tools.core :as it]))

(def default-opts
  {:handler/middleware [(ig/ref :matterjs.middleware.shape/get-shape)]})

(def config
  {[:essen/middleware :matterjs.middleware.shape/get-shape] {}

   [:essen/handler :matterjs.handler.shape/apply-force] default-opts
   [:essen/handler :matterjs.handler.shape/rotate] default-opts
   [:essen/handler :matterjs.handler.shape/scale] default-opts
   [:essen/handler :matterjs.handler.shape/set] default-opts
   [:essen/handler :matterjs.handler.shape/set-angle] default-opts
   [:essen/handler :matterjs.handler.shape/set-angularvelocity] default-opts
   [:essen/handler :matterjs.handler.shape/set-density] default-opts
   [:essen/handler :matterjs.handler.shape/set-inertia] default-opts
   [:essen/handler :matterjs.handler.shape/set-mass] default-opts
   [:essen/handler :matterjs.handler.shape/set-parts] default-opts
   [:essen/handler :matterjs.handler.shape/set-position] default-opts
   [:essen/handler :matterjs.handler.shape/set-static] default-opts
   [:essen/handler :matterjs.handler.shape/set-velocity] default-opts
   [:essen/handler :matterjs.handler.shape/set-vertices] default-opts
   [:essen/handler :matterjs.handler.shape/translate] default-opts
   [:essen/handler :matterjs.handler.shape/update] default-opts})

(def handlers
  (->> :essen/handler
       (it/find-derived-keys config)
       (map (comp ig/ref second))))

(defmethod ig/init-key :matterjs.middleware.shape/get-shape [_ _opts]
  (fn [_subs {:event/keys [shape] :as event} {:component/keys [shapes]}]
    (assoc event :event/body ((get-in shapes [shape :shape/body])))))

(defmethod ig/init-key :matterjs.handler.shape/apply-force [_ _opts]
  (fn [_context {:event/keys [body position force]} state _entity-state]
    (.applyForce Body body (clj->js position) (clj->js force))
    state))

(defmethod ig/init-key :matterjs.handler.shape/rotate [_ _opts]
  (fn [_context {:event/keys [body rotation points]} state _entity-state]
    (.rotate Body body rotation (clj->js points))
    state))

(defmethod ig/init-key :matterjs.handler.shape/scale [_ _opts]
  (fn [_context {:event/keys [body scale-x scale-y points]} state _entity-state]
    (.scale Body body scale-x scale-y (clj->js points))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set [_ _opts]
  (fn [_context {:event/keys [body settings value]} state _entity-state]
    (.set Body body (clj->js settings) (clj->js value))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-angle [_ _opts]
  (fn [_context {:event/keys [body angle]} state _entity-state]
    (.setAngle Body body angle)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-angularvelocity [_ _opts]
  (fn [_context {:event/keys [body velocity]} state _entity-state]
    (.setAngularVelocity Body body velocity)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-density [_ _opts]
  (fn [_context {:event/keys [body density]} state _entity-state]
    (.setDensity Body body density)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-inertia [_ _opts]
  (fn [_context {:event/keys [body inertia]} state _entity-state]
    (.setInertia Body body inertia)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-mass [_ _opts]
  (fn [_context {:event/keys [body mass]} state _entity-state]
    (.setMass Body body mass)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-parts [_ _opts]
  (fn [_context {:event/keys [body bodies auto-hull]} state _entity-state]
    (.setParts Body body (clj->js bodies) (or auto-hull true))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-position [_ _opts]
  (fn [_context {:event/keys [body position]} state _entity-state]
    (.setPosition Body body (clj->js position))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-static [_ _opts]
  (fn [_context {:event/keys [body is-static]} state _entity-state]
    (.setStatic Body body is-static)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-velocity [_ _opts]
  (fn [_context {:event/keys [body velocity]} state _entity-state]
    (.setVelocity Body body (clj->js velocity))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-vertices [_ _opts]
  (fn [_context {:event/keys [body vertices]} state _entity-state]
    (.setVertices Body body (clj->js vertices))
    state))

(defmethod ig/init-key :matterjs.handler.shape/translate [_ _opts]
  (fn [_context {:event/keys [body translation]} state _entity-state]
    (.translate Body body (clj->js translation))
    state))

(defmethod ig/init-key :matterjs.handler.shape/update [_ _opts]
  (fn [_context {:event/keys [body delta-time time-scale correction]} state _entity-state]
    (.update Body body delta-time time-scale correction)
    state))
