(ns feit.module.cljs-keyboard.interface
  (:require
   [feit.interface.keyboard.core :refer [FeitKeyboard]]))

(deftype CljsKeyboard [init-opts]
  FeitKeyboard)
