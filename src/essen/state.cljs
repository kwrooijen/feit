(ns essen.state
  (:require
   [clojure.spec.alpha :as s]
   [spec-signature.core :refer-macros [sdef]]))

(defonce state (atom {}))

(defonce game (atom {}))

(defonce messages (atom {}))

(defonce input-messages (atom {}))
