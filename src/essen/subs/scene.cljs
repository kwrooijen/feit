(ns essen.subs.scene
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::active-scenes
 (comp :active-scenes
       :essen))
