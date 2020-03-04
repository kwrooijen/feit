(ns essen.state
  (:require
   [clojure.spec.alpha :as s]
   [spec-signature.core :refer-macros [sdef]]))

(defonce state (atom {}))

(defonce game (atom {}))

(defonce messages (atom {}))

(defonce input-messages (atom {}))

(defn get-scene [scene-key]
  (get-in @state [:essen/scenes scene-key]))
