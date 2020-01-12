(ns cardo.core
  (:require
   [essen.core])
  (:require
   [lab.core]
   [lab.config]))

(defn ^:export init []
  (essen.core/init lab.config/config))

(defn stop []
  (essen.core/suspend!))

(defn start []
  (essen.core/resume lab.config/config))
