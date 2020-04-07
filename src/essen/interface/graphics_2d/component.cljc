(ns essen.interface.graphics-2d.component
  (:require
   [essen.methods :as es]
   [essen.interface.core :refer [define-schema!]]))

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


(defmethod es/schema-key :graphics-2d.component/sprite [_]
  [:or
   [:map
    :spritesheet/name
    [:spritesheet/frame {:optional true}]
    [:spritesheet/animation {:optional true}]]

   [:map
    :texture/name]])
