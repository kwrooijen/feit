(ns essen.re-frame
  (:require
   [re-frame.core :as re-frame]
   [essen.events.scene]
   [essen.subs.scene]))

(defn active-scenes []
  @(re-frame/subscribe [:essen.subs.scene/active-scenes]))
