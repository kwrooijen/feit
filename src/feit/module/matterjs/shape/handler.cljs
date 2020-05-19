(ns feit.module.matterjs.shape.handler
  (:require
   ["matter-js" :as Matter :refer [Body]]
   [integrant.core :as ig]
   [integrant-tools.core :as it]))

(def default-opts
  {})

(def config
  {[:feit/handler :matterjs.handler.shape/apply-force] default-opts
   [:feit/handler :matterjs.handler.shape/rotate] default-opts
   [:feit/handler :matterjs.handler.shape/scale] default-opts
   [:feit/handler :matterjs.handler.shape/set] default-opts
   [:feit/handler :matterjs.handler.shape/set-angle] default-opts
   [:feit/handler :matterjs.handler.shape/set-angularvelocity] default-opts
   [:feit/handler :matterjs.handler.shape/set-density] default-opts
   [:feit/handler :matterjs.handler.shape/set-inertia] default-opts
   [:feit/handler :matterjs.handler.shape/set-mass] default-opts
   [:feit/handler :matterjs.handler.shape/set-parts] default-opts
   [:feit/handler :matterjs.handler.shape/set-position] default-opts
   [:feit/handler :matterjs.handler.shape/set-static] default-opts
   [:feit/handler :matterjs.handler.shape/set-velocity] default-opts
   [:feit/handler :matterjs.handler.shape/set-vertices] default-opts
   [:feit/handler :matterjs.handler.shape/translate] default-opts
   [:feit/handler :matterjs.handler.shape/update] default-opts})

(def handlers
  (->> :feit/handler
       (it/find-derived-keys config)
       (map (comp ig/ref second))))

(defmethod ig/init-key :matterjs.handler.shape/apply-force [_ _opts]
  (fn [_context {:event/keys [position force]} {:keys [body] :as state}]
    (.applyForce Body body (clj->js position) (clj->js force))
    state))

(defmethod ig/init-key :matterjs.handler.shape/rotate [_ _opts]
  (fn [_context {:event/keys [rotation points]} {:keys [body] :as state}]
    (.rotate Body body rotation (clj->js points))
    state))

(defmethod ig/init-key :matterjs.handler.shape/scale [_ _opts]
  (fn [_context {:event/keys [scale-x scale-y points]} {:keys [body] :as state}]
    (.scale Body body scale-x scale-y (clj->js points))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set [_ _opts]
  (fn [_context {:event/keys [settings value]} {:keys [body] :as state}]
    (.set Body body (clj->js settings) (clj->js value))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-angle [_ _opts]
  (fn [_context {:event/keys [angle]} {:keys [body] :as state}]
    (.setAngle Body body angle)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-angularvelocity [_ _opts]
  (fn [_context {:event/keys [velocity]} {:keys [body] :as state}]
    (.setAngularVelocity Body body velocity)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-density [_ _opts]
  (fn [_context {:event/keys [density]} {:keys [body] :as state}]
    (.setDensity Body body density)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-inertia [_ _opts]
  (fn [_context {:event/keys [inertia]} {:keys [body] :as state}]
    (.setInertia Body body inertia)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-mass [_ _opts]
  (fn [_context {:event/keys [mass]} {:keys [body] :as state}]
    (.setMass Body body mass)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-parts [_ _opts]
  (fn [_context {:event/keys [bodies auto-hull]} {:keys [body] :as state}]
    (.setParts Body body (clj->js bodies) (or auto-hull true))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-position [_ _opts]
  (fn [_context {:event/keys [position]} {:keys [body] :as state}]
    (.setPosition Body body (clj->js position))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-static [_ _opts]
  (fn [_context {:event/keys [is-static]} {:keys [body] :as state}]
    (.setStatic Body body is-static)
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-velocity [_ _opts]
  (fn [_context {:event/keys [velocity]} {:keys [body] :as state}]
    (.setVelocity Body body (clj->js velocity))
    state))

(defmethod ig/init-key :matterjs.handler.shape/set-vertices [_ _opts]
  (fn [_context {:event/keys [vertices]} {:keys [body] :as state}]
    (.setVertices Body body (clj->js vertices))
    state))

(defmethod ig/init-key :matterjs.handler.shape/translate [_ _opts]
  (fn [_context {:event/keys [translation]} {:keys [body] :as state}]
    (.translate Body body (clj->js translation))
    state))

(defmethod ig/init-key :matterjs.handler.shape/update [_ _opts]
  (fn [_context {:event/keys [delta-time time-scale correction]} {:keys [body] :as state}]
    (.update Body body delta-time time-scale correction)
    state))

