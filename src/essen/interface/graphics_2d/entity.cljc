(ns essen.interface.graphics-2d.entity
  (:require
   [essen.methods :as es]
   [essen.interface.core :refer [define-schema!]]))

(defmethod es/ui-key :graphics-2d.entity/asset-loader [_ _schema]
  [:div])

(defmethod es/doc-key :graphics-2d.entity/asset-loader [_]
  "Entity to load assets.")

(define-schema! :g2d.asset-loader/files
  [:vector
   {:description ""}
   [:or
    'string?
    [:tuple 'qualified-keyword? string?]]])

(define-schema! :g2d.asset-loader/prefix
  ['string?
   {:description ""}])

(define-schema! :g2d.asset-loader/discard-extension?
  ['boolean?
   {:description ""}])

(define-schema! :g2d.asset-loader/discard-path?
  ['boolean?
   {:description ""}])

(define-schema! :g2d.asset-loader/next-scene
  ['qualified-keyword?
   {:description ""}])

(define-schema! :g2d.asset-loader/as
  ['qualified-keyword?
   {:description ""}])

(defmethod es/schema-key :graphics-2d.entity/asset-loader [_]
  [:map
   :g2d.asset-loader/files
   [:g2d.asset-loader/prefix
    {:optional true}]
   [:g2d.asset-loader/discard-extension?
    {:optional true :default true}]
   [:g2d.asset-loader/discard-path?
    {:optional true :default false}]
   :g2d.asset-loader/next-scene
   [:g2d.asset-loader/as
    {:optional true}]])

(define-schema! :g2d.spritesheet-loader/name
  ['qualified-keyword?
   {:description ""}])

(define-schema! :g2d.spritesheet-loader/file
  ['string?
   {:description ""}])

(define-schema! :g2d.spritesheet-loader/next-scene
  ['qualified-keyword?
   {:description ""}])

(defmethod es/schema-key :graphics-2d.entity/spritesheet-loader [_]
  [:map
   :g2d.spritesheet-loader/name
   :g2d.spritesheet-loader/file
   :g2d.spritesheet-loader/next-scene])
