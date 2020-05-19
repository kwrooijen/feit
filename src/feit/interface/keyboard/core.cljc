(ns feit.interface.keyboard.core
  (:require
   [integrant-tools.core :as it]
   [integrant.core :as ig]
   [meta-merge.core :refer [meta-merge]]
   [feit.config]
   [feit.state :as state]))

(defprotocol FeitKeyboard)

(deftype DefaultKeyboard []
  FeitKeyboard)

(def system
  :feit.interface.keyboard/system)

(defn init []
  (-> @feit.config/config
      (meta-merge {system {}})
      (ig/prep [system])
      (ig/init [system])
      (it/find-derived-value system)
      (or (DefaultKeyboard.))
      (state/set-keyboard!)))
