(ns rooij.interface.physics-2d.component
  (:require
   [rooij.methods :as es]
   [rooij.interface.core :refer [define-schema!]]))

(define-schema! :rectangle/x
  ['int?
   {:description ""}])


(define-schema! :rectangle/y
  ['int?
   {:description ""}])


(define-schema! :rectangle/w
  ['int?
   {:description ""}])


(define-schema! :rectangle/h
  ['int?
   {:description ""}])


(define-schema! :body/static?
  'boolean?)

(defmethod es/schema-key :physics-2d.component/rectangle [_]
  [:map
   :rectangle/x
   :rectangle/y
   :rectangle/w
   :rectangle/h

   :body/static? {:optional true :default false}])
