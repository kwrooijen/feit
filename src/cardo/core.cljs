(ns cardo.core
  (:require [phaser]
            [cardo.config :refer [config]]
            [integrant.core :as ig]
            [essen.core]))

(defmethod ig/init-key :my/update [_ opts]
  (fn [_this time]
    ;; (println "UPDATE" time)
    ))

(defmethod ig/init-key :my/update2 [_ opts]
  (fn [_this time]
    ;; (println "UPDATE2" time)
    ))

(defn ^:export init []
  (essen.core/init config))

(defn stop []
  (essen.core/suspend!))

(defn start []
  (essen.core/resume config))
