(ns rooij.interface.physics-2d.core
  (:require
   [rooij.config]
   [rooij.state :as state]
   [integrant.core :as ig]
   [integrant-tools.core :as it]))

(def system
  :rooij.interface.physics-2d/system)

(def scene
  :rooij.interface.physics-2d/scene)

(defn physics-2d-enabled? []
  (and (contains? (methods ig/init-key) system)
       (contains? (methods ig/init-key) scene)))

(defn init []
  (when (physics-2d-enabled?)
    (-> @rooij.config/config
        (ig/prep [system])
        (ig/init [system])
        (it/find-derived-value system)
        (state/set-physics-2d!))

    (state/set-physics-2d-scene!
     {:init (ig/init-key scene {})
      :halt! (ig/halt-key! scene {})})))

(it/derive-hierarchy
 {:physics-2d.component/rectangle [:rooij/component]})