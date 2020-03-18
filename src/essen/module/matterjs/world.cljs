(ns essen.module.matterjs.world
  (:refer-clojure :exclude [remove])

  (:require
   ["matter-js" :as Matter :refer [World]]
   [essen.module.matterjs.state :as state]))

(defn add [body]
  (.add World (.-world @state/engine) body))

(defn remove [body]
  (.remove World (.-world @state/engine) body))
