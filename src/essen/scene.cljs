(ns essen.scene
  (:require [integrant.core :as ig]
            [essen.state]))

(defmethod ig/init-key :essen/scene [[_ k] opts]

  {:key (:key opts)
   :preload (fn []
              (this-as this (swap! essen.state/this assoc k this))
              (this-as this (doall (map #(% this) (:modules/preload opts))))
              (:modules/preload opts))
   :create  (fn [] ;; (this-as this (doall (map #(% this) (:modules/create opts))))
              (:modules/create opts)
              )
   :update  (fn [time] (this-as this (doall (map #(% this time) (:modules/update opts)))))})

(defmethod ig/halt-key! :essen/scene [_ opts])

(defmethod ig/suspend-key! :essen/scene [_ opts])

(defmethod ig/resume-key :essen/scene [k opts old-opts old-impl]
  (if (not= opts old-opts)
    (ig/init-key k opts)
    old-impl))
