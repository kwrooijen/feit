(ns essen.render
  (:require
   [integrant.core :as ig]
   [essen.state :as state]))

(defn render-key []
  (first (descendants :essen.module.spawn/render)))

(defn init [scene-key]
  (ig/init-key (render-key)
               {:essen/config @state/config
                :essen/scene scene-key}))

(defn halt! [scene-key]
  (ig/halt-key! (render-key)
                {:essen/config @state/config
                 :essen/scene scene-key}))

(defn resume [scene-key]
  (ig/resume-key (render-key)
                  {:essen/config @state/config
                   :essen/scene scene-key}))

(defn suspend! [scene-key]
  (ig/suspend-key! (render-key)
                   {:essen/config @state/config
                    :essen/scene scene-key}))
