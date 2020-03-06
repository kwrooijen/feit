(ns essen.state)

(defonce state (atom {}))

(defonce game (atom {}))

(defonce messages (atom {}))

(defonce input-messages (atom {}))

(defonce persistent-entities (atom {}))

(defn get-scene [scene-key]
  (get-in @state [:essen/scenes scene-key]))
