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
  (fn matterjs-middleware-shape--get-shape
    [{:context/keys [state]}
     {:event/keys [shape] :as event}]
    (assoc event :event/body ((get-in (:component/shapes state) [shape :shape/body])))))

(defmethod ig/init-key :matterjs.handler.shape/apply-force [_ _opts]
  (fn [_context {:event/keys [body position force]} state]
    (.applyForce Body body (clj->js position) (clj->js force))
    state))

(defmethod ig/init-key :matterjs.handler.shape/rotate [_ _opts]
  (fn [_context {:event/keys [body rotation points]} state]
    (.rotate Body body rotation (clj->js points))
    state))

(defmethod ig/init-key :matterjs.handler.shape/scale [_ _opts]
  (fn [_context {:event/keys [body scale-x scale-y points]} state]
    (.scale Body body scale-x scale-y (clj->js points))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set [_ _opts]
  (fn [_context {:event/keys [body settings value]} state]
    (.set Body body (clj->js settings) (clj->js value))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-angle [_ _opts]
  (fn [_context {:event/keys [body angle]} state]
    (.setAngle Body body angle)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-angularvelocity [_ _opts]
  (fn [_context {:event/keys [body velocity]} state]
    (.setAngularVelocity Body body velocity)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-density [_ _opts]
  (fn [_context {:event/keys [body density]} state]
    (.setDensity Body body density)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-inertia [_ _opts]
  (fn [_context {:event/keys [body inertia]} state]
    (.setInertia Body body inertia)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-mass [_ _opts]
  (fn [_context {:event/keys [body mass]} state]
    (.setMass Body body mass)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-parts [_ _opts]
  (fn [_context {:event/keys [body bodies auto-hull]} state]
    (.setParts Body body (clj->js bodies) (or auto-hull true))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-position [_ _opts]
  (fn [_context {:event/keys [body position]} state]
    (.setPosition Body body (clj->js position))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-static [_ _opts]
  (fn [_context {:event/keys [body is-static]} state]
    (.setStatic Body body is-static)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-velocity [_ _opts]
  (fn [_context {:event/keys [body velocity]} state]
    (.setVelocity Body body (clj->js velocity))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-vertices [_ _opts]
  (fn [_context {:event/keys [body vertices]} state]
    (.setVertices Body body (clj->js vertices))
    state))

(defmethod ig/init-key :matterjs.handler.shape/translate [_ _opts]
  (fn [_context {:event/keys [body translation]} state]
    (.translate Body body (clj->js translation))
    state))

(defmethod ig/init-key :matterjs.handler.shape/update [_ _opts]
  (fn [_context {:event/keys [body delta-time time-scale correction]} state]
    (.update Body body delta-time time-scale correction)
    state))
