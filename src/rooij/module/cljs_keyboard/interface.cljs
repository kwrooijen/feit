(ns rooij.module.cljs-keyboard.interface
  (:require
   [rooij.interface.keyboard.core :refer [RooijKeyboard]]))

(deftype CljsKeyboard [init-opts]
  RooijKeyboard)
