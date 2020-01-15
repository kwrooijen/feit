(ns cardo.core
  (:require
   [essen.core])
  (:require
   ;; [lab.core]
   ;; [lab.config]
   [rng.core]
   [rng.config]))

(defn ^:export init []
  (essen.core/init rng.config/config))

(defn stop []
  (essen.core/suspend!))

(defn start []
  (essen.core/resume rng.config/config))
