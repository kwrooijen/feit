(ns essen.render
  (:require
   [essen.state :as state :refer [game]]))

(defn run [scene-key type]
  ((-> @game :essen.module/render type) (:essen/config @game) scene-key))
