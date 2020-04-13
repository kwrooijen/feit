(ns rooij.interface.graphics-2d.component
  (:require
   [rooij.methods :as es]
   [rooij.interface.core :refer [define-schema!]]))

(define-schema! :spritesheet/name
  ['qualified-keyword?
   {:description ""}])

(define-schema! :spritesheet/frame
  ['qualified-keyword?
   {:description ""}])

(define-schema! :spritesheet/animation
  [:or 'qualified-keyword? 'string?])

(define-schema! :texture/name
  ['qualified-keyword?
   {:description ""}])


(define-schema! :shape/x
  ['number?
   {:description ""}])

(define-schema! :shape/y
  ['number?
   {:description ""}])

(define-schema! :shape/w
  ['number?
   {:description ""}])

(define-schema! :shape/h
  ['number?
   {:description ""}])

(define-schema! :shape/fill
  ['number?
   {:description ""}])

(defmethod es/schema-key :graphics-2d.component/sprite [_]
  [:or
   [:map
    :spritesheet/name
    [:spritesheet/frame {:optional true}]
    [:spritesheet/animation {:optional true}]]

   [:map
    :texture/name]])

(defmethod es/schema-key :graphics-2d.component/rectangle [_]
  [:map
   :shape/x
   :shape/y
   :shape/w
   :shape/h
   :shape/fill])
